package com.stan.blog.beans.entity.content;

import java.io.Serializable;
import java.util.Objects;

public class ContentTagId implements Serializable {
    private String contentId;
    private Long tagId;

    public ContentTagId() {}
    public ContentTagId(String contentId, Long tagId) {
        this.contentId = contentId;
        this.tagId = tagId;
    }
    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }
    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentTagId that = (ContentTagId) o;
        return Objects.equals(contentId, that.contentId) && Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentId, tagId);
    }
}