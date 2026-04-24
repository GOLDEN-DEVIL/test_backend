package com.readit.backend.service;

import com.readit.backend.dto.ContactRequest;
import com.readit.backend.entity.ContactMessage;
import com.readit.backend.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ContactMessage submitMessage(ContactRequest request) {
        ContactMessage message = modelMapper.map(request, ContactMessage.class);
        return contactMessageRepository.save(message);
    }
}
