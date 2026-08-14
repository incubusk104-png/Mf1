import { PolicyLayout, type PolicySection } from "@/components/PolicyLayout";
import { site } from "@/lib/site";

const sections: PolicySection[] = [
  {
    heading: "1. Who we are",
    paragraphs: [
      `"Mindset Frames" is a mood-first habit tracking app for Android (package name ${site.packageName}), published on Huawei AppGallery by ${site.developerName} ("we", "us", "our"). This Privacy Policy explains what data the Mindset Frames app and this website process, why, and the choices you have.`,
      `You can reach us anytime at ${site.supportEmail}.`,
    ],
  },
  {
    heading: "2. The short version",
    bullets: [
      "Mindset Frames is local-first: your habits, check-ins, moods, and reflections are stored on your device by default.",
      "Cloud backup is optional and only starts after you create an account and sign in.",
      "We show no ads, embed no advertising or analytics SDKs, and never sell or share your data for marketing.",
      "You can permanently delete your account and all cloud data from inside the app at any time.",
      "The app server is hosted outside the Chinese mainland.",
    ],
    paragraphs: [],
  },
  {
    heading: "3. Data stored on your device",
    paragraphs: [
      "The app keeps your habits, daily check-ins, mood history, one-line reflections, earned badges, companion avatar, and settings in private app storage on your device. This data never leaves your device unless you enable cloud backup. Uninstalling the app removes it.",
    ],
  },
  {
    heading: "4. Data processed when you enable cloud backup",
    paragraphs: ["If you choose to create an account, we process the minimum needed to run backup and restore:"],
    bullets: [
      "Email sign-in: your email address and a password. The password is stored only as a salted hash by our authentication provider — we never see or store it in plain text.",
      "HUAWEI ID sign-in: with your consent, the minimal scopes only — your OpenID identifier and email address. The sign-in is verified server-side with Huawei's account service. We do not access your contacts, phone number, or any other HUAWEI ID profile data.",
      "Backup content: your habits, check-in dates, mood history, and app settings, linked to a random account identifier.",
      "A random per-install device identifier (UUID) used for sync bookkeeping. It is not an advertising ID and is not derived from any hardware identifier.",
    ],
  },
  {
    heading: "5. Purposes and legal bases",
    paragraphs: [
      "We process account data to provide backup, restore, and multi-device sync (performance of a contract), to keep your account secure (legitimate interest), and only after your explicit action to sign in (consent, which you may withdraw by signing out or deleting the account). We do not profile you, and we make no automated decisions about you.",
    ],
  },
  {
    heading: "6. Where your data is stored",
    paragraphs: [
      "Cloud data is stored in a managed PostgreSQL database operated by Supabase, hosted on servers located outside the Chinese mainland. All data in transit is protected with TLS 1.2 or higher. On your device, session tokens are sealed with a hardware-backed key in the Android Keystore.",
    ],
  },
  {
    heading: "7. Third-party services",
    paragraphs: ["We rely on two service providers, each receiving only what is necessary:"],
    bullets: [
      "Supabase (authentication and database hosting) — receives your account email or Huawei-derived account identity and your encrypted-in-transit backup content.",
      "Huawei Account Kit (optional HUAWEI ID sign-in) — processes your HUAWEI ID sign-in under Huawei's own privacy statement; we receive only the verified OpenID and email.",
    ],
  },
  {
    heading: "8. Retention and deletion",
    paragraphs: [
      "Cloud data is retained while your account exists. Choosing \"Delete account\" in Settings permanently erases every cloud record you own — habits, check-ins, moods, settings, and the account itself — in a single server-side transaction. Local data stays on your device until you clear the app's storage or uninstall.",
    ],
  },
  {
    heading: "9. Your rights",
    paragraphs: [
      "Depending on your region (including the GDPR in the EEA/UK and PIPL-style rights elsewhere), you have the right to access, correct, export, restrict, or delete your personal data, and to withdraw consent at any time. Most of these are self-service inside the app; for anything else, email us and we will respond within 30 days. You may also lodge a complaint with your local supervisory authority.",
    ],
  },
  {
    heading: "10. Children",
    paragraphs: [
      "Mindset Frames is not directed at children under 16, and we do not knowingly collect personal data from children. If you believe a child has created an account, contact us and we will delete it. Minors should use the app's optional account features only with a guardian's consent.",
    ],
  },
  {
    heading: "11. App permissions",
    paragraphs: ["The app requests only what its features need:"],
    bullets: [
      "Notifications — for the daily reminders, streak alerts, and weekly recaps you configure. All are generated on-device; there is no push server.",
      "Internet & network state — used solely for the optional cloud backup.",
      "Boot completed — to re-schedule your reminders after a device restart.",
      "No camera, microphone, location, contacts, or storage access beyond saving a share image you explicitly export.",
    ],
  },
  {
    heading: "12. Security",
    paragraphs: [
      "Every cloud row is isolated per account with strict row-level security; sign-in tokens are encrypted at rest on your device; HUAWEI ID sign-ins are verified server-side against Huawei's account service before any account is issued; and all connections use HTTPS. No method of storage is 100% secure, but we design so that even our own server code can access only what it must.",
    ],
  },
  {
    heading: "13. Changes to this policy",
    paragraphs: [
      "If we change this policy, the new version will be posted on this page with an updated effective date. Material changes will also be announced inside the app before they take effect.",
    ],
  },
  {
    heading: "14. Contact",
    paragraphs: [
      `Developer: ${site.developerName}`,
      `App: Mindset Frames (${site.packageName})`,
      `Email: ${site.supportEmail}`,
      `Website: ${site.domain}`,
    ],
  },
];

export default function PrivacyPolicy() {
  return (
    <PolicyLayout
      title="Privacy Policy"
      updatedLabel={`Effective date: ${site.privacyEffectiveDate} · Mindset Frames`}
      backLabel="Back to mindsetframes.online"
      altVersionLabel="简体中文版本"
      altVersionTo="/privacy/zh"
      sections={sections}
    />
  );
}
