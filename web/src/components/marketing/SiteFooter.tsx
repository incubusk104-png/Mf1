import { Link } from "react-router-dom";
import { Globe2, Mail } from "lucide-react";

import { AppGalleryCta } from "@/components/marketing/AppGalleryCta";
import { Button } from "@/components/ui/button";
import { useI18n } from "@/lib/i18n";
import { site } from "@/lib/site";

export function SiteFooter() {
  const { t } = useI18n();

  return (
    <footer className="border-t border-border">
      <section className="mx-auto max-w-6xl px-5 py-20 text-center">
        <h2 className="font-display text-4xl font-semibold tracking-tight sm:text-5xl">
          {t.privacyBandTitle}
        </h2>
        <p className="mx-auto mt-4 max-w-2xl text-lg leading-relaxed text-muted-foreground">
          {t.privacyBandBody}
        </p>
        <div className="mt-8 flex flex-wrap items-center justify-center gap-4">
          <AppGalleryCta />
          <Button asChild variant="outline" size="lg" className="h-14 rounded-full px-7">
            <Link to="/privacy">{t.privacyBandCta}</Link>
          </Button>
        </div>
      </section>

      <div className="border-t border-border">
        <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-6 px-5 py-10 md:flex-row">
          <div className="flex items-center gap-2.5">
            <img src="/assets/logo.png" alt={`${site.name} logo`} className="h-7 w-7 object-contain" />
            <div>
              <p className="font-display font-semibold leading-none">{site.name}</p>
              <p className="mt-1 text-xs text-muted-foreground">{t.footerTagline}</p>
            </div>
          </div>

          <nav className="flex flex-wrap items-center justify-center gap-x-6 gap-y-2 text-sm text-muted-foreground">
            <Link to="/privacy" className="transition-colors hover:text-foreground">
              {t.footerPrivacyEn}
            </Link>
            <Link to="/privacy/zh" className="transition-colors hover:text-foreground">
              {t.footerPrivacyZh}
            </Link>
            <a
              href={`mailto:${site.supportEmail}`}
              className="inline-flex items-center gap-1.5 transition-colors hover:text-foreground"
            >
              <Mail className="h-3.5 w-3.5" aria-hidden />
              {t.footerSupport}
            </a>
          </nav>
        </div>

        <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-2 px-5 pb-10 text-xs text-muted-foreground md:flex-row">
          <p className="inline-flex items-center gap-1.5">
            <Globe2 className="h-3.5 w-3.5" aria-hidden />
            {t.footerServerNote}
          </p>
          <p>
            © {new Date().getFullYear()} {site.name}. {t.footerRights}
          </p>
        </div>
      </div>
    </footer>
  );
}
