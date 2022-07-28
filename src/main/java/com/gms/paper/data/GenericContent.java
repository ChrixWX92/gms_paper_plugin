package com.gms.paper.data;

import java.util.Arrays;

public class GenericContent extends DBContent {
    public String contentId;
    private transient boolean _cached = false;

    private transient String _partialContentId = null;

    public boolean isCached() {
        return _cached;
    }

    protected <T extends GenericContent> T setCached() {
        assert !_cached;
        _cached = true;
        CmsApi.s_public.updateContent(contentId, this);
        return (T)this;
    }

    @Override
    public String toString() {
        return contentId;
    }

    public String partialContentId() {
        if (_partialContentId != null && !_partialContentId.isEmpty())
            return _partialContentId;

        String[] parts = contentId.split("\\.");

        if (parts.length > 0)
            parts = Arrays.copyOf(parts, parts.length - 1);

        _partialContentId = String.join(".", parts);

        return _partialContentId;
    }

    public String cleanContentId() {
        return contentId.replace(".", "");
    }
}
