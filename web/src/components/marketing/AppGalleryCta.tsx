import { useCallback } from "react";

import { useI18n } from "@/lib/i18n";
import { appGalleryUrl, site } from "@/lib/site";
import { toast } from "@/components/ui/sonner";

/**
 * Official "Explore it on AppGallery" badge. Links to the live listing once
 * published; until then it shows an honest "launching soon" toast instead of
 * ever exposing a dead store link.
 */
export function AppGalleryCta({ className = "" }: { className?: string }) {
  const { t } = useI18n();

  const onClick = useCallback(
    (event: React.MouseEvent<HTMLAnchorElement>) => {
      if (!site.appGalleryLive) {
        event.preventDefault();
        toast(t.heroComingSoonToast);
      }
    },
    [t],
  );

  return (
    <a
      href={site.appGalleryLive ? appGalleryUrl : "#"}
      onClick={onClick}
      target={site.appGalleryLive ? "_blank" : undefined}
      rel="noopener noreferrer"
      aria-label={t.heroBadgeAlt}
      className={`group inline-flex flex-col items-center transition-transform hover:scale-[1.03] active:scale-[0.98] ${className}`}
    >
      <img
        src="/assets/appgallery-badge.png"
        alt={t.heroBadgeAlt}
        className="h-14 w-auto rounded-xl shadow-lg shadow-black/40"
        loading="eager"
      />
      {!site.appGalleryLive && (
        <span className="mt-1.5 text-[11px] font-medium uppercase tracking-widest text-muted-foreground">
          {t.heroComingSoon}
        </span>
      )}
    </a>
  );
}
