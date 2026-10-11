package com.trello.project.client.exception.dto;

public record RemoteApiError(
        String detail,
        Object fields,
        String instance,
        String message,
        Integer status,
        String title,
        String type) {
}