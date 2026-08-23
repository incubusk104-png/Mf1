// habit-recommend — server-side proxy to Google Gemini's free API tier for
// AI habit suggestions. The Gemini key lives ONLY here (Supabase secret),
// never in the app. One free key is shared across every user of this app,
// so this function enforces a small per-user daily cap on top of Google's
// own rate limit — a few abusive users must never exhaust everyone else's
// free quota for the day. On any failure (quota hit, network, bad response)
// this returns a clear "unavailable" signal so the app can fall back to its
// on-device rule-based recommender instead of showing an error.

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const MAX_CALLS_PER_USER_PER_DAY = 3;
const GEMINI_MODEL = "gemini-2.5-flash";
const GEMINI_URL =
  `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent`;

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

interface RequestBody {
  existing_habits?: string[];
}

function adminClient() {
  const url = Deno.env.get("SUPABASE_URL");
  const key = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!url || !key) throw new Error("Missing Supabase credentials");
  return createClient(url, key);
}

async function verifyCaller(supabase: ReturnType<typeof adminClient>, authHeader: string | null) {
  const token = authHeader?.replace(/^Bearer /i, "");
  if (!token) return null;
  const { data, error } = await supabase.auth.getUser(token);
  if (error || !data.user) return null;
  return data.user;
}

/** True if under today's per-user cap; increments the counter as a side effect. */
async function checkAndIncrementUsage(
  supabase: ReturnType<typeof adminClient>,
  userId: string,
): Promise<boolean> {
  const today = new Date().toISOString().slice(0, 10);
  const { data } = await supabase
    .from("ai_recommend_usage")
    .select("usage_date, call_count")
    .eq("user_id", userId)
    .maybeSingle();

  if (!data || data.usage_date !== today) {
    await supabase
      .from("ai_recommend_usage")
      .upsert({ user_id: userId, usage_date: today, call_count: 1 });
    return true;
  }

  if (data.call_count >= MAX_CALLS_PER_USER_PER_DAY) return false;

  await supabase
    .from("ai_recommend_usage")
    .update({ call_count: data.call_count + 1 })
    .eq("user_id", userId);
  return true;
}

function buildPrompt(existingHabits: string[]): string {
  const habitList = existingHabits.length > 0
    ? existingHabits.join(", ")
    : "(none yet)";
  return (
    "You are a habit-tracking assistant. The user currently tracks these " +
    `habits: ${habitList}. Suggest exactly 5 NEW habits they don't already ` +
    "track, that would round out a balanced routine (mix of health, mind, " +
    "productivity, social, finance where sensible). " +
    "Reply with ONLY a JSON array, no markdown, no prose, in this exact " +
    'shape: [{"name": "Drink water", "reason": "short reason, under 12 words"}]'
  );
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response(null, { headers: corsHeaders });

  try {
    const supabase = adminClient();
    const user = await verifyCaller(supabase, req.headers.get("authorization"));
    if (!user) {
      return new Response(JSON.stringify({ error: "unauthorized" }), {
        status: 401,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const allowed = await checkAndIncrementUsage(supabase, user.id);
    if (!allowed) {
      return new Response(JSON.stringify({ error: "daily_limit_reached" }), {
        status: 429,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const geminiKey = Deno.env.get("GEMINI_API_KEY");
    if (!geminiKey) {
      return new Response(JSON.stringify({ error: "ai_unavailable" }), {
        status: 503,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const body: RequestBody = await req.json().catch(() => ({}));
    // Cap input size so one weird request can't burn an outsized share of
    // the shared daily free quota — 30 names is already more than plenty
    // of context for a good suggestion.
    const existingHabits = (body.existing_habits ?? []).slice(0, 30);

    const geminiResponse = await fetch(`${GEMINI_URL}?key=${geminiKey}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [{ parts: [{ text: buildPrompt(existingHabits) }] }],
        generationConfig: { temperature: 0.7, maxOutputTokens: 400 },
      }),
    });

    if (!geminiResponse.ok) {
      // Free-tier rate limit or outage — let the app fall back gracefully.
      return new Response(JSON.stringify({ error: "ai_unavailable" }), {
        status: 503,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const geminiData = await geminiResponse.json();
    const text: string | undefined =
      geminiData?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!text) {
      return new Response(JSON.stringify({ error: "ai_unavailable" }), {
        status: 503,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Strip any accidental markdown fences before parsing.
    const cleaned = text.replace(/```json|```/g, "").trim();
    let suggestions: unknown;
    try {
      suggestions = JSON.parse(cleaned);
    } catch {
      return new Response(JSON.stringify({ error: "ai_unavailable" }), {
        status: 503,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    return new Response(JSON.stringify({ suggestions }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (e) {
    console.error("habit-recommend error:", e instanceof Error ? e.message : e);
    return new Response(JSON.stringify({ error: "ai_unavailable" }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
