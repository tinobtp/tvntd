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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.tvntd.lib.ObjectId;

@Entity
@Inheritance
@DiscriminatorColumn(name = "ArtType")
@DiscriminatorValue(value = "Article")
@Table(indexes = {
    @Index(columnList = "authorUuid", name = "authorUuid", unique = false)
})
public class Article
{
    /*
     * contentOId format:
     * HEX:abcdef123... for OID
     * DOC:docs.google.com for doc link.
     * VID:youtube.com for video link.
     */
    public static int MaxTitleLength = 128;
    public static int MaxContentLength = 1 << 16;
    public static int DOC_TYPE = 100;
    public static int VID_TYPE = 200;
    public static int DRV_TYPE = 300;

    @Id
    @Column(length = 64)
    protected String   articleUuid;

    @Column(length = 64)
    protected String   authorUuid;

    protected Long     authorId;
    protected boolean  pending;

    @Column(length = 64)
    protected String contentOId;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    protected Date createdDate;

    @Column(length = 128)
    protected byte[] topic;

    @Column(length = 128)
    protected byte[] publicTag;

    @Lob
    @Column(length = 1 << 16)
    protected byte[] content;

    @Column(length = 64)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ArtPics",
            joinColumns = @JoinColumn(name = "articleId"))
    protected List<String> pictures;

    @Transient
    private byte[] contentBrief;

    public Article()
    {
        super();
        articleUuid = UUID.randomUUID().toString();
        createdDate = new Date();
    }

    public Article(String uuid)
    {
        super();
        articleUuid = uuid;
        createdDate = new Date();
    }

    public void markPending() {
        pending = true;
    }

    public void markActive() {
        pending = false;
    }

    static public String makeUrlLink(Article self, String host, int mode)
    {
        String oid;

        if (mode == DOC_TYPE) {
            oid = "DOC:" + host;
        } else if (mode == VID_TYPE) {
            oid = "VID:" + host;
        } else if (mode == DRV_TYPE) {
            oid = "DRV:" + host;
        } else {
            oid = "HEX:" + host;
        }
        if (self != null) {
            self.contentOId = oid;
        }
        return oid;
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

    public boolean isPending() {
        return pending;
    }

    public void addPicture(ObjectId img)
    {
        if (pictures == null) {
            pictures = new ArrayList<>();
        }
        pictures.add(img.name());
    }

    public void removePicture(ObjectId img)
    {
        if (pictures != null) {
            pictures.remove(img.name());
        }
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
     * @return the articleUuid
     */
    public String getArticleUuid() {
        return articleUuid;
    }

    /**
     * @param articleUuid the articleUuid to set
     */
    public void setArticleUuid(String articleUuid) {
        this.articleUuid = articleUuid;
    }

    /**
     * @return the contentOId
     */
    public String getContentOId() {
        return contentOId;
    }

    /**
     * @param contentOId the contentOId to set
     */
    public void setContentOId(String contentOId) {
        this.contentOId = contentOId;
    }

    /**
     * @return the createdDate
     */
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * @param createdDate the createdDate to set
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * @return the topic
     */
    public byte[] getTopic() {
        return topic;
    }

    /**
     * @param topic the topic to set
     */
    public void setTopic(byte[] topic) {
        this.topic = topic;
    }

    /**
     * @return the publicTag
     */
    public byte[] getPublicTag() {
        return publicTag;
    }

    /**
     * @return the content
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * @return the pictures
     */
    public List<String> getPictures() {
        return pictures;
    }

    /**
     * @param pictures the pictures to set
     */
    public void setPictures(List<String> pictures) {
        this.pictures = pictures;
    }

    /**
     * @return the contentBrief
     */
    public byte[] getContentBrief() {
        return contentBrief;
    }

    /**
     * @param contentBrief the contentBrief to set
     */
    public void setContentBrief(byte[] contentBrief) {
        this.contentBrief = contentBrief;
    }
}
