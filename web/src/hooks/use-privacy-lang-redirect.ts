import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

const OVERRIDE_KEY = "mf_privacy_lang_choice";

/** Auto-redirects between /privacy and /privacy/zh based on browser language.
 *  Solves Huawei's mainland-China localized-policy compliance flag. */
export function usePrivacyLangRedirect(pageLang: "en" | "zh") {
  const navigate = useNavigate();

  useEffect(() => {
    if (sessionStorage.getItem(OVERRIDE_KEY)) return;
    const isZhBrowser = navigator.language?.toLowerCase().startsWith("zh");
    const shouldBeZh = isZhBrowser ? "zh" : "en";
    if (shouldBeZh !== pageLang) {
      navigate(shouldBeZh === "zh" ? "/privacy/zh" : "/privacy", { replace: true });
    }
  }, [navigate, pageLang]);
}
