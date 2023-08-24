package com.pu.programus.bridge;

import com.pu.programus.member.Member;
import com.pu.programus.position.Position;
import com.pu.programus.project.Project;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class MemberProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Project project;
}
