package com.trello.project.service;

import com.trello.project.repositories.MemberRepository;
import org.springframework.stereotype.Service;

import com.trello.project.entities.Member;

@Service
public class MemberProjectServiceImpl implements MemberProjectService {

    private final MemberRepository memberRepository;

    MemberProjectServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }

}
