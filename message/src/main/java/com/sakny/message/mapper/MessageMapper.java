package com.sakny.message.mapper;

import com.sakny.message.dto.response.MessageResponse;
import com.sakny.message.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(source = "conversation.id",       target = "conversationId")
    @Mapping(source = "sender.id",             target = "senderId")
    @Mapping(source = "sender.name",           target = "senderName")
    @Mapping(source = "receiver.id",           target = "receiverId")
    @Mapping(source = "receiver.name",         target = "receiverName")
    @Mapping(source = "read",                  target = "isRead")
    MessageResponse toResponse(Message message);
}
