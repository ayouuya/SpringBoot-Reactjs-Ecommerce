package com.cart.ecom_proj.repo;

import com.cart.ecom_proj.model.AppUser;
import com.cart.ecom_proj.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepo extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserOrderByTimestampAsc(AppUser user);
    void deleteByUser(AppUser user);
}
