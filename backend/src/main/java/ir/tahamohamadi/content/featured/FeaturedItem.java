package ir.tahamohamadi.content.featured;

import ir.tahamohamadi.blog.post.BlogPost;
import ir.tahamohamadi.common.persistence.AuditedSoftDeletableEntity;
import ir.tahamohamadi.portfolio.project.PortfolioProject;
import ir.tahamohamadi.publication.Publication;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity @Table(name="featured_item") @Getter @NoArgsConstructor(access=AccessLevel.PROTECTED)
public class FeaturedItem extends AuditedSoftDeletableEntity {
    @Column(name="slot_key",nullable=false) private String slotKey;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="blog_post_id") private BlogPost blogPost;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="portfolio_project_id") private PortfolioProject portfolioProject;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="publication_id") private Publication publication;
    @Column(name="sort_order",nullable=false) private int sortOrder;
    @Column(name="starts_at") private Instant startsAt;
    @Column(name="ends_at") private Instant endsAt;
    @Column(name="is_active",nullable=false) private boolean active;
    private FeaturedItem(UUID id,String slot,BlogPost post,PortfolioProject project,Publication publication,int order,Instant starts,Instant ends,Instant at){ initialize(id,at); apply(slot,post,project,publication,order,starts,ends,at); active=true; }
    public static FeaturedItem create(UUID id,String slot,BlogPost post,PortfolioProject project,Publication publication,int order,Instant starts,Instant ends,Instant at){return new FeaturedItem(id,slot,post,project,publication,order,starts,ends,at);}
    public void update(String slot,BlogPost post,PortfolioProject project,Publication publication,int order,Instant starts,Instant ends,Instant at){ apply(slot,post,project,publication,order,starts,ends,at); }
    public void activate(Instant at){active=true; updatedAt=Objects.requireNonNull(at,"at");}
    public void deactivate(Instant at){active=false; updatedAt=Objects.requireNonNull(at,"at");}
    private void apply(String slot,BlogPost post,PortfolioProject project,Publication publication,int order,Instant starts,Instant ends,Instant at){
        slotKey=requireNonBlank(slot,"slotKey");
        if((post!=null?1:0)+(project!=null?1:0)+(publication!=null?1:0)!=1)throw new IllegalArgumentException("exactly one target is required");
        if(order<0)throw new IllegalArgumentException("sortOrder must be nonnegative");
        if(starts!=null&&ends!=null&&ends.isBefore(starts))throw new IllegalArgumentException("endsAt must not precede startsAt");
        blogPost=post; portfolioProject=project; this.publication=publication; sortOrder=order; startsAt=starts; endsAt=ends; updatedAt=Objects.requireNonNull(at,"at");
    }
}
