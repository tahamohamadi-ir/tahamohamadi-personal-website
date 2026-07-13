package ir.tahamohamadi.content.social.api.publicsite;

import ir.tahamohamadi.content.social.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
public class PublicSocialLinkService {
    private final SocialLinkRepository links;
    public PublicSocialLinkService(SocialLinkRepository links) { this.links = links; }
    public PublicSocialLinksResponse list() { return new PublicSocialLinksResponse(links.findTop50ByActiveTrueAndDeletedAtIsNullOrderBySortOrderAscIdAsc().stream().map(link -> new PublicSocialLinkResponse(link.getPlatformCode(),link.getUrl(),link.getSortOrder())).toList()); }
}
