package com.library.repository;
import com.library.model.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void insert(Member member) {
        entityManager.persist(member);
    }

    public Optional<Member> getMemberById(Long memberId) {
        return Optional
                .ofNullable(entityManager
                        .find(Member.class, memberId));
    }

    public List<Member> getAllMembers() {
        String jpql = "select m from Member m";
        return entityManager
                .createQuery(jpql, Member.class)
                .getResultList();
    }
}