package com.pu.programus.member;

import com.pu.programus.bridge.MemberProject;
import com.pu.programus.position.Position;
import com.pu.programus.project.Project;
import com.pu.programus.projectApply.ProjectApply;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // key

    @Column(nullable = false, unique = true)
    private String uid; // 아이디
    @Column(nullable = false)
    private String password; // 패스워드
    @Column(nullable = false)
    private String userName; // 닉네임
    private String department; // 소속
    private String email; // 이메일
    private String intro; // 소개
    private String contents; // 본문 소개

    @ManyToOne(fetch = FetchType.LAZY)
    private Position position; // 카테고리로 바꾸기

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Project> ownerProjects = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL) // mappedBy로 참조하는 외래키임을 명시
    private List<MemberProject> memberProjects = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<ProjectApply> AppliedProjects = new ArrayList<>();

    //Todo: 변경하기??
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return this.uid;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void addMemberProject(MemberProject memberProject) {
        memberProjects.add(memberProject);
        memberProject.setMember(this);
    }
}
