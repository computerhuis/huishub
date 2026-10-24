package com.github.computerhuis.huishub.web;

import jakarta.faces.context.FacesContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UtilBean {

    public static String getRedirectUrl(final Long id) {
        val viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        if (isNotBlank(viewId)) {
            val params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            if (!params.containsKey("id")) {
                return viewId + "?faces-redirect=true&id=%d".formatted(id);
            } else {
                return viewId + "?faces-redirect=true";
            }
        }
        return null;
    }
}
