package com.datn.fashion.service;

public enum ImageUploadTarget {
    STYLE("nova-atelier/styles"), OUTFIT("nova-atelier/outfits"), AVATAR("nova-atelier/users/avatars"), WARDROBE("nova-atelier/users/wardrobe");
    private final String folder;
    ImageUploadTarget(String folder) { this.folder = folder; }
    public String folder() { return folder; }
}
