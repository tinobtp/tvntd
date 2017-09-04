/*
 * Copyright (C) 2014-2015 Vy Nguyen
 * Github https://github.com/vy-nguyen/tvntd
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.tvntd.models;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tvntd.forms.ArticleForm;
import com.tvntd.forms.CommentChangeForm;
import com.tvntd.key.HashKey;
import com.tvntd.util.Constants;
import com.tvntd.util.Util;

@Entity
@Table(indexes = {
    @Index(columnList = "authorUuid", unique = false),
    @Index(columnList = "publicUrlOid", unique = false)
})
public class ArticleRank
{
    public static int MaxTitleLength = 128;
    public static int MaxContentLength = 250;
    public static Long PERM_PRIVATE = 0x80000000L;

    static private Logger s_log = LoggerFactory.getLogger(ArticleRank.class);

    @Id
    @Column(length = 64)
    private String articleUuid;

    @Column(length = 64)
    private String tagHash;

    @Column(length = 64)
    private byte[] tag;

    @Column(length = 64)
    private String authorUuid;

    @Column(length = 64)
    private String publicUrlOid;

    @Column(length = 64)
    private String urlTag;

    @Column(length = 64)
    private String imageOid;

    @Column(length = 64)
    private String prevArticle;

    @Column(length = 64)
    private String nextArticle;

    @Column(length = 64)
    private String topArticle;

    @Column(length = 128)
    private byte[] artTitle;

    @Column(length = 256)
    private byte[] contentBrief;

    private Date timeStamp;
    private Long creditEarned;
    private Long moneyEarned;
    private Long likes;
    private Long shared;
    private Long rank;
    private Long score;
    private Long permMask;
    private Long authorId;
    private boolean favorite;
    private boolean hasArticle;

    /**
     * Contains value from ArtTag to specify article type.
     */
    private String artTag;
    private String contentOid;
    private String contentLinkUrl;

    /*
     * Reserve to record head of a transaction block chain.
     */
    private String transRoot;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ArticleLiked",
            joinColumns = @JoinColumn(name = "articleId"))
    private List<String> userLiked;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ArticleShared",
            joinColumns = @JoinColumn(name = "articleId"))
    private List<String> userShared;

    public ArticleRank()
    {
        this.creditEarned = 0L;
        this.moneyEarned  = 0L;
        this.likes        = 0L;
        this.shared       = 0L;
        this.score        = 0L;
        this.rank         = 0L;
        this.favorite     = false;
        this.artTitle     = Util.DefaultTopic;
        this.contentBrief = null;
        this.timeStamp    = new Date();
        this.tag          = Util.DefaultTag;
    }

    public ArticleRank(String artUuid)
    {
        this();
        this.articleUuid = artUuid;
    }

    /**
     * Create article rank from article.
     */
    public ArticleRank(AuthorTag tag, Article article)
    {
        this();
        this.artTag       = ArtTag.BLOG;
        this.articleUuid  = article.getArticleUuid();
        this.authorUuid   = article.getAuthorUuid();
        this.favorite     = tag.isFavorite();
        this.rank         = tag.getRank();
        this.artTitle     = article.getTopic();
        this.tag          = tag.fetchTag();
        this.tagHash      = HashKey.toSha1Key(this.tag, authorUuid);
        this.contentBrief = article.getContentBrief();
    }

    public ArticleRank(Article article, String content,
            String host, String url, String tag, int mode)
    {
        this();
        artTag      = ArtTag.BLOG;
        articleUuid = article.getArticleUuid();
        authorUuid  = article.getAuthorUuid();
        artTitle    = article.getTopic();
        this.tag    = Util.toRawByte(tag, 64);
        tagHash     = HashKey.toSha1Key(this.tag, authorUuid);

        List<String> imgs = article.getPictures();
        if (imgs != null && !imgs.isEmpty()) {
            imageOid = imgs.get(0);
        }
        if (host != null && url != null) {
            hasArticle     = false;
            contentLinkUrl = url;
            contentOid     = Article.makeUrlLink(null, host, mode);
        } else {
            hasArticle = true;
        }
        contentBrief = Util.toRawByte(content, MaxContentLength);
    }

    /**
     * Create article rank from product.
     */
    public ArticleRank(AuthorTag tag, Product product)
    {
        this();
        this.artTag      = ArtTag.ESTORE;
        this.articleUuid = product.getArticleUuid();
        this.authorUuid  = product.getAuthorUuid();
        this.favorite    = tag.isFavorite();
        this.rank        = tag.getRank();
        this.artTitle    = product.getProdName();
        this.tag         = product.getProdCat();
        this.tagHash     = HashKey.toSha1Key(this.tag, authorUuid);

        this.contentBrief =
            Arrays.copyOfRange(product.getProdDesc(), 0, MaxContentLength);
    }

    /**
     * Create article rank from ads.
     */
    public ArticleRank(AuthorTag tag, AdsPost ads)
    {
        this();
        this.artTag       = ArtTag.ADS;
        this.articleUuid  = ads.getArticleUuid();
        this.authorUuid   = ads.getAuthorUuid();
        this.favorite     = tag.isFavorite();
        this.rank         = tag.getRank();
        this.contentBrief = Arrays.copyOfRange(ads.getBusDesc(), 0, MaxContentLength);
        this.artTitle     = ads.getBusName();
        this.tag          = ads.getBusCat();
        this.tagHash      = HashKey.toSha1Key(this.tag, authorUuid);
    }

    public ArticleRank(CommentChangeForm form, String authorUuid)
    {
        this();
        this.artTag       = form.getKind();
        this.articleUuid  = form.getArticleUuid();
        this.authorUuid   = authorUuid;
        this.favorite     = form.isFavorite();
        this.creditEarned = form.getCommentId();
    }

    public void updateFromUser(ArticleForm form)
    {
        String title = form.getTitle();
        if (artTitle == null) {
            artTitle = Util.DefaultTopic;
        }
        if (title != null && !title.isEmpty()) {
            artTitle = title.getBytes(Charset.forName("UTF-8"));
        }
        favorite = form.isFavorite();
        rank = form.getArticleRank();

        if (form.getTagName() != null) {
            tag = form.getTagName().getBytes(Charset.forName("UTF-8"));
        }
        Long likeCnt = form.getLikeInc();
        if (likeCnt > 0) {
            likes++;
            // TODO: update userLiked
            //
        } else if (likeCnt < 0 && likes > 0) {
            likes--;
        }

        Long shareCnt = form.getShareInc();
        if (shareCnt > 0) {
            shared++;
        } else if (shareCnt < 0 && shared > 0) {
            shared--;
        }
        if (form.getPrevArticle() != null) {
            prevArticle = form.getPrevArticle();
        }
        if (form.getNextArticle() != null) {
            nextArticle = form.getNextArticle();
        }
        if (form.getTopArticle() != null) {
            topArticle = form.getTopArticle();
        }
    }

    static public String toUrlTag(String urlTag, String topic)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("https://tudoviet.com/public/article/")
            .append(Util.utf8ToUrlString(urlTag))
            .append("/")
            .append(Util.utf8ToUrlString(topic));
        return sb.toString();
    }

    public boolean setPublicUrl(String publicTag)
    {
        if (artTitle == null) {
            return false;
        }
        urlTag = Util.utf8ToUrlString(publicTag);
        try {
            String title = Util.utf8ToUrlString(new String(artTitle, "UTF-8"));
            publicUrlOid = HashKey.toSha1Key(urlTag, title);
            s_log.debug("Convert " + urlTag + ", " + title + ": " + publicUrlOid);

        } catch(UnsupportedEncodingException e) {
        }
        return false;
    }

    public void markActive() {
        this.permMask = 0L;
    }

    public void markPending() {
        this.permMask = PERM_PRIVATE;
    }

    /**
     * @return the articleId
     */
    public String getArticleUuid() {
        return articleUuid;
    }

    /**
     * @param articleId the articleId to set
     */
    public void setArticleId(String articleUuid) {
        this.articleUuid = articleUuid;
    }

    /**
     * @return the tag
     */
    public String getTag()
    {
        if (tag != null) {
            try {
                return new String(tag, "UTF-8");

            } catch(UnsupportedEncodingException e) {
            }
        }
        return Constants.DefaultTag;
    }

    public void setTag(String newTag) {
        this.tag = newTag.getBytes(Charset.forName("UTF-8"));
    }

    /**
     * @return the authorUuid
     */
    public String getAuthorUuid() {
        return authorUuid;
    }

    /**
     * @param authorUuid the authorUuid to set
     */
    public void setAuthorUuid(String authorUuid) {
        this.authorUuid = authorUuid;
    }

    /**
     * @return the publicUrlOid
     */
    public String getPublicUrlOid() {
        return publicUrlOid;
    }

    /**
     * @param publicUrlOid the publicUrlOid to set
     */
    public void setPublicUrlOid(String publicUrlOid) {
        this.publicUrlOid = publicUrlOid;
    }

    /**
     * @return the urlTag
     */
    public String getUrlTag() {
        return urlTag;
    }

    /**
     * @param urlTag the urlTag to set
     */
    public void setUrlTag(String urlTag) {
        this.urlTag = urlTag;
    }

    /**
     * @return the imageOid
     */
    public String getImageOid() {
        return imageOid;
    }

    /**
     * @param imageOid the imageOid to set
     */
    public void setImageOid(String imageOid) {
        this.imageOid = imageOid;
    }

    /**
     * @return the prevArticle
     */
    public String getPrevArticle() {
        return prevArticle;
    }

    /**
     * @param prevArticle the prevArticle to set
     */
    public void setPrevArticle(String prevArticle) {
        this.prevArticle = prevArticle;
    }

    /**
     * @return the nextArticle
     */
    public String getNextArticle() {
        return nextArticle;
    }

    /**
     * @param nextArticle the nextArticle to set
     */
    public void setNextArticle(String nextArticle) {
        this.nextArticle = nextArticle;
    }

    /**
     * @return the topArticle
     */
    public String getTopArticle() {
        return topArticle;
    }

    /**
     * @param topArticle the topArticle to set
     */
    public void setTopArticle(String topArticle) {
        this.topArticle = topArticle;
    }

    /**
     * @return the artTitle
     */
    public String getArtTitle()
    {
        return artTitle == null ?
            "Post" : new String(artTitle, Charset.forName("UTF-8"));
    }

    /**
     * @param artTitle the artTitle to set
     */
    public void setArtTitle(String artTitle)
    {
        if (artTitle == null) {
            artTitle = "Post";
        }
        this.artTitle = artTitle.getBytes(Charset.forName("UTF-8"));
    }

    /**
     * @return the contentBrief
     */
    public String getContentBrief()
    {
        if (contentBrief != null) {
            return new String(contentBrief, Charset.forName("UTF-8"));
        }
        return "...";
    }

    public byte[] fetchContentBrief() {
        return contentBrief;
    }

    /**
     * @return the timeStamp
     */
    public Date getTimeStamp() {
        return timeStamp;
    }

    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * @return the creditEarned
     */
    public Long getCreditEarned() {
        return creditEarned;
    }

    /**
     * @param creditEarned the creditEarned to set
     */
    public void setCreditEarned(Long creditEarned) {
        this.creditEarned = creditEarned;
    }

    /**
     * @return the moneyEarned
     */
    public Long getMoneyEarned() {
        return moneyEarned;
    }

    /**
     * @param moneyEarned the moneyEarned to set
     */
    public void setMoneyEarned(Long moneyEarned) {
        this.moneyEarned = moneyEarned;
    }

    /**
     * @return the likes
     */
    public Long getLikes() {
        return likes;
    }

    /**
     * @param likes the likes to set
     */
    public void setLikes(Long likes) {
        this.likes = likes;
    }

    /**
     * @return the shared
     */
    public Long getShared() {
        return shared;
    }

    /**
     * @param shared the shared to set
     */
    public void setShared(Long shared) {
        this.shared = shared;
    }

    /**
     * @return the rank
     */
    public Long getRank() {
        return rank;
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(Long rank) {
        this.rank = rank;
    }

    /**
     * @return the score
     */
    public Long getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(Long score) {
        this.score = score;
    }

    /**
     * @return the permMask
     */
    public Long getPermMask() {
        return permMask;
    }

    /**
     * @return the authorId
     */
    public Long getAuthorId() {
        return authorId;
    }

    /**
     * @param authorId the authorId to set
     */
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    /**
     * @return the favorite
     */
    public boolean isFavorite() {
        return favorite;
    }

    /**
     * @param favorite the favorite to set
     */
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    /**
     * @return the userLiked
     */
    public List<String> getUserLiked() {
        return userLiked;
    }

    /**
     * @param userLiked the userLiked to set
     */
    public void setUserLiked(List<String> userLiked) {
        this.userLiked = userLiked;
    }

    /**
     * @return the userShared
     */
    public List<String> getUserShared() {
        return userShared;
    }

    /**
     * @param userShared the userShared to set
     */
    public void setUserShared(List<String> userShared) {
        this.userShared = userShared;
    }

    /**
     * @return the hasArticle
     */
    public boolean isHasArticle() {
        return hasArticle;
    }

    /**
     * @param hasArticle the hasArticle to set
     */
    public void setHasArticle(boolean hasArticle) {
        this.hasArticle = hasArticle;
    }

    /**
     * @return the artTag
     */
    public String getArtTag() {
        return artTag;
    }

    /**
     * @param artTag the artTag to set
     */
    public void setArtTag(String artTag) {
        this.artTag = artTag;
    }

    /**
     * @return the contentOid
     */
    public String getContentOid() {
        return contentOid;
    }

    /**
     * @param contentOid the contentOid to set
     */
    public void setContentOid(String contentOid) {
        this.contentOid = contentOid;
    }

    /**
     * @return the contentLinkUrl
     */
    public String getContentLinkUrl() {
        return contentLinkUrl;
    }

    /**
     * @param contentLinkUrl the contentLinkUrl to set
     */
    public void setContentLinkUrl(String contentLinkUrl) {
        this.contentLinkUrl = contentLinkUrl;
    }

    /**
     * @return the transRoot
     */
    public String getTransRoot() {
        return transRoot;
    }

    /**
     * @param transRoot the transRoot to set
     */
    public void setTransRoot(String transRoot) {
        this.transRoot = transRoot;
    }
}
