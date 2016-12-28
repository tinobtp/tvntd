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
package com.tvntd.service.user;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.tvntd.dao.ArticleRankRepo;
import com.tvntd.dao.ArticleRepository;
import com.tvntd.forms.CommentChangeForm;
import com.tvntd.forms.PostForm;
import com.tvntd.forms.UuidForm;
import com.tvntd.models.Article;
import com.tvntd.models.ArticleRank;
import com.tvntd.service.api.IArticleService;
import com.tvntd.service.api.IAuthorService;
import com.tvntd.service.api.ICommentService;
import com.tvntd.service.api.IProfileService.ProfileDTO;
import com.tvntd.util.Util;

@Service
@Transactional
public class ArticleService implements IArticleService
{
    static private Logger s_log = LoggerFactory.getLogger(ArticleService.class);

    @Autowired
    protected ArticleRepository articleRepo;

    @Autowired
    protected ArticleRankRepo artRankRepo;

    @Autowired
    protected IAuthorService authorSvc;

    @Autowired
    protected ICommentService commentSvc;

    /**
     * Common static methods.
     */
    public static void applyPostForm(PostForm form, Article art, boolean publish)
    {
        if (publish == true) {
            art.markActive();
        } else {
            art.markPending();
        }
        try {
            String str = Util.truncate(form.getTopic(), Article.MaxTitleLength);
            art.setTopic(str.getBytes("UTF-8"));

            str = Util.truncate(form.getContent(), Article.MaxContentLength);
            art.setContent(str.getBytes("UTF-8"));
            if (form.getContentBrief() != null) {
                str = Util.truncate(form.getContentBrief(), ArticleRank.MaxContentLength);
                art.setContentBrief(str.getBytes("UTF-8"));
            }
        } catch(UnsupportedEncodingException e) {
            s_log.info(e.getMessage());
        }
    }

    public static Article toArticle(PostForm form, ProfileDTO profile, boolean pub)
    {
        Article art = new Article();
        art.setAuthorId(profile.fetchUserId());
        art.setAuthorUuid(profile.getUserUuid());
        ArticleService.applyPostForm(form, art, false);
        return art;
    }

    public static void toArticleRank(ArticleDTO art, ProfileDTO author, String tag)
    {
    }

    /**
     * Utilities to convert to DTO forms.
     */
    @Override
    public List<ArticleDTO> convert(List<Article> arts)
    {
        List<ArticleDTO> result = new LinkedList<>();
        for (Article at : arts) {
            result.add(new ArticleDTO(at, getRank(at.getArticleUuid())));
        }
        return result;
    }

    @Override
    public List<ArticleRankDTO> convertRank(List<ArticleRank> ranks)
    {
        List<ArticleRankDTO> result = new LinkedList<>();
        for (ArticleRank r : ranks) {
            result.add(new ArticleRankDTO(r));
        }
        return result;
    }

    /**
     * Article ranking.
     */
    @Override
    public ArticleRank getRank(String artUuid)
    {
        return artRankRepo.findByArticleUuid(artUuid);
    }

    @Override
    public List<ArticleRank> getArtRank(String[] artUuids)
    {
        List<ArticleRank> result = new LinkedList<>();

        for (String uuid : artUuids) {
            ArticleRank rank = artRankRepo.findByArticleUuid(uuid);
            if (rank != null) {
                result.add(rank);
            }
        }
        return result;
    }

    @Override
    public ArticleRank updateRank(CommentChangeForm form, ProfileDTO me)
    {
        String artUuid = form.getArticleUuid();
        String myUuid = me.getUserUuid();
        String kind = form.getKind();

        ArticleRank rank = artRankRepo.findByArticleUuid(artUuid);
        if (rank == null) {
            Article article = articleRepo.findByArticleUuid(artUuid);
            if (article != null) {
                return null;
            }
            rank = authorSvc.createArticleRank(article, "Comment");
            if (rank == null) {
                return null;
            }
        }
        boolean save = false;
        
        if (kind.equals("like")) {
            Long val = rank.getLikes();
            List<String> users = rank.getUserLiked();

            if (users == null) {
                users = new ArrayList<>();
                rank.setUserLiked(users);
            }
            save = true;
            if (Util.<String>isInList(users, myUuid) == null) {
                val++;
                Util.<String>addUnique(users, myUuid);
            } else {
                val--;
                Util.<String>removeFrom(users, myUuid);
            }
            rank.setLikes(val);
            form.setAmount(val);

        } else if (kind.equals("share")) {
            Long val = rank.getShared();
            List<String> users = rank.getUserShared();

            if (users == null) {
                users = new ArrayList<>();
                rank.setUserShared(users);
            }
            save = true;
            if (Util.<String>isInList(users, myUuid) == null) {
                val++;
                Util.<String>addUnique(users, myUuid);
            } else {
                val--;
                Util.<String>removeFrom(users, myUuid);
            }
            rank.setShared(val);
            form.setAmount(val);

        } else if (kind.equals("fav")) {
        }
        if (save == true) {
            artRankRepo.save(rank);
        }
        return rank;
    }

    public List<ArticleRankDTO> getArticleRank(UuidForm uuids)
    {
        List<ArticleRankDTO> ranks = new LinkedList<>();

        for (String uuid : uuids.getUuids()) {
            List<ArticleRank> r = artRankRepo.findByAuthorUuid(uuid);

            if (r != null && !r.isEmpty()) {
                ranks.addAll(convertRank(r));
            }
        }
        return ranks;
    }

    /**
     * Article services.
     */
    @Override
    public ArticleDTO getArticleDTO(String artUuid)
    {
        String uuid = artUuid.toString();
        Article art = articleRepo.findByArticleUuid(uuid);
        if (art != null) {
            return new ArticleDTO(art, getRank(uuid));
        }
        return null;
    }

    @Override
    public Article getArticle(String artUuid) {
        return articleRepo.findByArticleUuid(artUuid);
    }

    @Override
    public List<ArticleDTO> getArticles(List<String> uuids)
    {
        List<ArticleDTO> ret = new LinkedList<>();

        for (String uuid : uuids) {
            ArticleDTO dto = getArticleDTO(uuid);
            if (dto != null) {
                ret.add(dto);
            }
        }
        return ret;
    }

    protected Map<String, ArticleRank> getRanks(List<Article> articles)
    {
        Map<String, ArticleRank> ranks = new HashMap<>();
        for (Article art : articles) {
            String uuid = art.getArticleUuid();
            ArticleRank rank = artRankRepo.findByArticleUuid(uuid);
            if (rank == null) {
                continue;
            }
            ranks.put(uuid, rank);
        }
        return ranks;
    }

    @Override
    public List<ArticleDTO> getArticlesByUser(Long userId)
    {
        List<Article> articles =
            articleRepo.findAllByAuthorIdOrderByCreatedDateDesc(userId);
        return convert(articles);
    }

    @Override
    public List<ArticleDTO> getArticlesByUser(String userUuid)
    {
        List<Article> articles =
            articleRepo.findAllByAuthorUuidOrderByCreatedDateAsc(userUuid.toString());
        return convert(articles);
    }

    @Override
    public List<ArticleDTO> getArticlesByUser(List<String> uuidList)
    {
        List<ArticleDTO> result = new LinkedList<>();

        for (String userUuid : uuidList) {
            List<ArticleDTO> arts = getArticlesByUser(userUuid);
            if (arts != null && !arts.isEmpty()) {
                result.addAll(arts);
            }
        }
        return result;
    }

    @Override
    public Page<ArticleDTO> getUserArticles(Long userId)
    {
        Pageable req = new PageRequest(0, 10, new Sort(Sort.Direction.DESC, "created"));
        Page<Article> page =
            articleRepo.findByAuthorIdOrderByCreatedDateDesc(userId, req);
        List<Article> articles = page.getContent();

        return new PageImpl<ArticleDTO>(
                convert(articles), req, page.getTotalElements());
    }

    @Override
    public Page<ArticleDTO> getUserArticles(String userUuid)
    {
        Pageable req = new PageRequest(0, 10, new Sort(Sort.Direction.DESC, "created"));
        Page<Article> page =
            articleRepo.findByAuthorUuidOrderByCreatedDateDesc(userUuid, req);
        List<Article> articles = page.getContent();

        return new PageImpl<ArticleDTO>(
                convert(articles), req, page.getTotalElements());
    }

    @Override
    public void saveArtRank(List<ArticleRank> ranks)
    {
        for (ArticleRank r : ranks) {
            artRankRepo.save(r);
        }
    }

    @Override
    public void saveArticle(ArticleDTO article) {
        articleRepo.save(article.fetchArticle());
    }

    @Override
    public void saveArticle(Article article) {
        articleRepo.save(article);
    }

    @Override
    public boolean deleteArticle(Article art, ProfileDTO owner)
    {
        if (!art.getAuthorUuid().equals(owner.getUserUuid())) {
            return false;
        }
        ArticleRank rank = artRankRepo.findByArticleUuid((art.getArticleUuid()));
        if (rank != null) {
            artRankRepo.delete(rank);
        }
        articleRepo.delete(art.getArticleUuid());
        return true;
    }

    @Override
    public boolean deleteArticle(String uuid, ProfileDTO owner)
    {
        s_log.info("Delete article " + uuid + ", owner " + owner.getUserUuid());

        ArticleDTO art = getArticleDTO(uuid);
        if (art != null) {
            Article article = art.fetchArticle();
            if (!owner.getUserUuid().equals(article.getAuthorUuid())) {
                s_log.info("Wrong owner " + owner.getUserUuid());
                return false;
            }
            ArticleRank rank = artRankRepo.findByArticleUuid(article.getArticleUuid());
            if (rank != null) {
                artRankRepo.delete(rank);
            }
            articleRepo.delete(article);
            return true;
        }
        return false;
    }

    @Override
    public void saveArticles(String jsonFile, String rsDir)
    {
        s_log.info("Save articles");
    }
}
