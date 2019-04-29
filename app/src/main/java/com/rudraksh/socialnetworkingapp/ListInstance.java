package com.rudraksh.socialnetworkingapp;

public class ListInstance {

    String profileUri,username,dateOfPosting,comment;

    public ListInstance(String profileUri, String username, String dateOfPosting, String comment) {
        this.profileUri = profileUri;
        this.username = username;
        this.dateOfPosting = dateOfPosting;
        this.comment = comment;
    }

    public String getProfileUri() {
        return profileUri;
    }

    public String getUsername() {
        return username;
    }

    public String getdateOfPosting() {
        return dateOfPosting;
    }

    public String getComment() {
        return comment;
    }
}
