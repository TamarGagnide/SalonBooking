package com.salonbooking.api.dto;

import com.salonbooking.domain.Stylist;

public record StylistResponse(Long id, String name, String nameKa) {
    public static StylistResponse from(Stylist stylist) {
        return new StylistResponse(stylist.getId(), stylist.getName(), stylist.getNameKa());
    }
}
