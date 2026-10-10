package com.library.service;

import com.library.enums.MembershipType;
import com.library.model.Member;
import com.library.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void insert(String name, String email, MembershipType membershipType) {
        Member member = new Member();
        member.setName(name);
        member.setEmail(email);
        member.setMembershipType(membershipType);
        memberRepository.insert(member);
    }

    public Optional<Member> getMemberById(Long memberId) {
        return memberRepository.getMemberById(memberId);
    }

    public List<Member> getAllMembers() {
        return memberRepository.getAllMembers();
    }
}