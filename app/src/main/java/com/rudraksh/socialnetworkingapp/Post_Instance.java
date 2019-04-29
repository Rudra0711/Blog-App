package com.rudraksh.socialnetworkingapp;


public class Post_Instance {

    private String profileDp,postPic,username,desc,likeCount,commentCount,time;

    public Post_Instance(String profileDp, String postPic, String username, String desc, String likeCount, String commentCount, String time) {
        this.profileDp = profileDp;
        this.postPic = postPic;
        this.username = username;
        this.time = time;
        this.desc = desc;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

    public String getProfileDp() {
        return profileDp;
    }

    public String getPostPic() {
        return postPic;
    }

    public String getUsername() {
        return username;
    }

    public String getTime() {
        return time;
    }

    public String getDesc() {
        return desc;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public String getCommentCount() {
        return commentCount;
    }
}
