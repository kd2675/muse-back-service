package muse.back.service.database.pub.dto;

public record FollowStatusResponse(Long artistId, boolean following, long followerCount) {}
