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
package com.tvntd.service.api;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.tvntd.lib.ObjectId;
import com.tvntd.models.Profile;
import com.tvntd.models.User;
import com.tvntd.objstore.ObjStore;

public interface IProfileService
{
    public ProfileDTO getProfile(Long userId);
    public ProfileDTO getProfile(UUID uuid);

    public List<ProfileDTO> getProfileList(List<UUID> userIds);
    public List<ProfileDTO> getProfileList(ProfileDTO user, List<Profile> raw);
    public Page<ProfileDTO> getProfileList();

    public List<ProfileDTO> followProfiles(ProfileDTO me, String[] uuids);
    public List<ProfileDTO> connectProfiles(ProfileDTO me, String[] uuids);

    public void updateWholeProfile(ProfileDTO profile);
    public void updateProfiles(List<ProfileDTO> profiles);
    public void saveUserImgUrl(ProfileDTO profile, ObjectId oid);
    public void createProfile(User user);
    public void deleteProfile(Long userId);

    public static Comparator<UUID> s_uuidCompare = new Comparator<UUID>() {
        @Override
        public int compare(UUID u1, UUID u2) {
            return u1.compareTo(u2);
        }
    };

    public static class ProfileDTO
    {
        private Profile profile;

        private String coverImg0;
        private String coverImg1;
        private String coverImg2;
        private String userImgUrl;
        private String transRoot;
        private String mainRoot;
        private String userUrl;

        private Long connections;
        private Long follows;
        private Long followers;
        private Long chainCount;
        private Long creditEarned;
        private Long creditIssued;
        private Long moneyEarned;
        private Long moneyIssued;

        public ProfileDTO(Profile prof)
        {
            profile = prof;

            String baseUri = "/rs/upload";
            ObjStore objStore = ObjStore.getInstance();
            coverImg0 = objStore.imgObjUri(prof.getCoverImg0(), baseUri);
            coverImg1 = objStore.imgObjUri(prof.getCoverImg1(), baseUri);
            coverImg2 = objStore.imgObjUri(prof.getCoverImg2(), baseUri);
            userImgUrl = objStore.imgObjUri(prof.getUserImgUrl(), baseUri);

            transRoot = prof.getTransRoot().name();
            mainRoot = prof.getTransRoot().name();
            userUrl = "/user/id/" + profile.getUserUuid().toString();

            creditEarned = 200L;
            creditIssued = 300L;
            moneyEarned = 500L;
            moneyIssued = 400L;

            connections = Long.valueOf(prof.getConnectList().size());
            follows = Long.valueOf(prof.getFollowList().size());
            followers = Long.valueOf(prof.getFollowerList().size());
            chainCount = Long.valueOf(prof.getChainLinks().size());

            if (coverImg0 == null) {
                coverImg0 = "/rs/img/demo/s1.jpg";
                coverImg1 = "/rs/img/demo/s2.jpg";
                coverImg2 = "/rs/img/demo/s3.jpg";
            }
            if (userImgUrl == null) {
                userImgUrl = "/rs/img/avatars/male.png";
            }
        }

        public Profile toProfile() {
            return profile;
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();

            sb.append(profile.getLocale()).append(", firstName ")
                .append(profile.getFirstName()).append('\n')
                .append("Name: ").append(profile.getUserName())
                .append("Uuid: ").append(profile.getUserUuid().toString()).append('\n');
            return sb.toString();
        }

        /**
         * @return the userId.  Use this so that it won't show up in JSON.
         */
        public Long obtainUserId() {
            return profile.getUserId();
        }

        /**
         * Common code for UUID list.
         */
        protected UUID findUuid(List<UUID> src, UUID item)
        {
            synchronized(this) {
                for (UUID uuid : src) {
                    if (uuid.equals(item)) {
                        return uuid;
                    }
                }
            }
            return null;
        }

        protected void removeUuid(List<UUID> src, UUID giving)
        {
            synchronized(this) {
                Iterator<UUID> iter = src.iterator();
                while (iter.hasNext()) {
                    UUID item = iter.next();
                    if (item.equals(giving)) {
                        iter.remove();
                        break;
                    }
                }
            }
        }

        protected void addUuidList(List<UUID> src, UUID add)
        {
            synchronized(this) {
                src.add(add);
            }
        }

        /**
         * Connect this profile to the peer profile if there's mutual agreement.
         */
        public void connectProfile(ProfileDTO peer)
        {
            boolean connected = false;
            UUID myUuid = getUserUuid();
            UUID peerUuid = peer.getUserUuid();
            UUID mine = findUuid(profile.getConnectList(), peerUuid);
            UUID his = peer.findUuid(peer.getConnectList(), myUuid);

            if (mine != null) {
                if (his == null) {
                    peer.addUuidList(peer.getConnectList(), myUuid);
                }
                connected = true;
            } else {
                if (his != null) {
                    addUuidList(getConnectList(), peerUuid);
                }
                connected = true;
            }
            if (connected == true) {
                peer.removeUuid(peer.getFollowList(), myUuid);
                peer.removeUuid(peer.getFollowerList(), myUuid);
                removeUuid(getFollowList(), peerUuid);
                removeUuid(getFollowerList(), peerUuid);
                return;
            }
            followProfile(peer);
        }

        public void unConnectProfile(ProfileDTO peer)
        {
            removeUuid(peer.getConnectList(), getUserUuid());
            removeUuid(getConnectList(), peer.getUserUuid());
        }

        public void followProfile(ProfileDTO peer)
        {
            if (findUuid(getFollowList(), peer.getUserUuid()) == null) {
                addUuidList(getFollowList(), peer.getUserUuid());
            }
            if (findUuid(peer.getFollowerList(), getUserUuid()) == null) {
                peer.addUuidList(peer.getFollowerList(), getUserUuid());
            }
        }

        public void unfollowProfile(ProfileDTO peer)
        {
            removeUuid(getFollowList(), peer.getUserUuid());
            peer.removeUuid(peer.getFollowerList(), getUserUuid());
        }

        /**
         * Convert list of Profile to ProfileDTO.
         */
        public static List<ProfileDTO> convert(List<Profile> list)
        {
            List<ProfileDTO> result = new LinkedList<>();

            if (list != null) {
                for (Profile prof : list) {
                    result.add(new ProfileDTO(prof));
                }
            }
            return result;
        }

        /**
         * @return the locale
         */
        public String getLocale() {
            return profile.getLocale();
        }

        /**
         * @param locale the locale to set
         */
        public void setLocale(String locale) {
            profile.setLocale(locale);
        }

        /**
         * @return the userName
         */
        public String getUserName() {
            return profile.getUserName();
        }

        /**
         * @param userName the userName to set
         */
        public void setUserName(String userName) {
            profile.setUserName(userName);
        }

        /**
         * @return the firstName
         */
        public String getFirstName() {
            return profile.getFirstName();
        }

        /**
         * @param firstName the firstName to set
         */
        public void setFirstName(String firstName) {
            profile.setFirstName(firstName);
        }

        /**
         * @return the lastName
         */
        public String getLastName() {
            return profile.getLastName();
        }

        /**
         * @param lastName the lastName to set
         */
        public void setLastName(String lastName) {
            profile.setLastName(lastName);
        }

        /**
         * @return the userRole
         */
        public String getUserRole() {
            return null;
        }

        /**
         * @return the userStatus
         */
        public String getUserStatus() {
            return null;
        }

        /**
         * @return the userUrl
         */
        public String getUserUrl() {
            return userUrl;
        }

        /**
         * @return the coverImg0
         */
        public String getCoverImg0() {
            return coverImg0;
        }

        /**
         * @param coverImg0 the coverImg0 to set
         */
        public void setCoverImg0(String coverImg0) {
            this.coverImg0 = coverImg0;
        }

        /**
         * @return the coverImg1
         */
        public String getCoverImg1() {
            return coverImg1;
        }

        /**
         * @param coverImg1 the coverImg1 to set
         */
        public void setCoverImg1(String coverImg1) {
            this.coverImg1 = coverImg1;
        }

        /**
         * @return the coverImg2
         */
        public String getCoverImg2() {
            return coverImg2;
        }

        /**
         * @param coverImg2 the coverImg2 to set
         */
        public void setCoverImg2(String coverImg2) {
            this.coverImg2 = coverImg2;
        }

        /**
         * @return the transRoot
         */
        public String getTransRoot() {
            return transRoot;
        }

        /**
         * @return the mainRoot
         */
        public String getMainRoot() {
            return mainRoot;
        }

        /**
         * @return the userUuid
         */
        public UUID getUserUuid() {
            return profile.getUserUuid();
        }

        /**
         * @return the userImgUrl
         */
        public String getUserImgUrl() {
            return userImgUrl;
        }

        /**
         * @param userImgUrl the userImgUrl to set
         */
        public void setUserImgUrl(String userImgUrl) {
            this.userImgUrl = userImgUrl;
        }

        /**
         * @return the connectList
         */
        public List<UUID> getConnectList() {
            return profile.getConnectList();
        }

        /**
         * @return the followList
         */
        public List<UUID> getFollowList() {
            return profile.getFollowList();
        }

        /**
         * @return the followerList
         */
        public List<UUID> getFollowerList() {
            return profile.getFollowerList();
        }

        /**
         * @return the chainLinks
         */
        public List<Long> getChainLinks() {
            return profile.getChainLinks();
        }

        /**
         * @return the connections
         */
        public Long getConnections() {
            return connections;
        }

        /**
         * @return the follows
         */
        public Long getFollows() {
            return follows;
        }

        /**
         * @return the followers
         */
        public Long getFollowers() {
            return followers;
        }

        /**
         * @return the chainCount
         */
        public Long getChainCount() {
            return chainCount;
        }

        /**
         * @return the creditEarned
         */
        public Long getCreditEarned() {
            return creditEarned;
        }

        /**
         * @return the creditIssued
         */
        public Long getCreditIssued() {
            return creditIssued;
        }

        /**
         * @return the moneyEarned
         */
        public Long getMoneyEarned() {
            return moneyEarned;
        }

        /**
         * @return the moneyIssued
         */
        public Long getMoneyIssued() {
            return moneyIssued;
        }
    }
}
