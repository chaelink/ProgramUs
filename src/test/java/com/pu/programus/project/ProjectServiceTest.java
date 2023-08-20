package com.pu.programus.project;

import com.pu.programus.location.Location;
import com.pu.programus.position.Position;
import com.pu.programus.position.PositionRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
@Transactional
public class ProjectServiceTest {
    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    PositionRepository positionRepository;

    @Autowired
    ProjectHeadCountRepository projectHeadCountRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("프로젝트 요약 정보 가져오기")
    void 프로젝트_목록_가져오기() {

        Project project1 = new Project();
        Project project2 = new Project();
        Project project3 = new Project();

        Location location = new Location();
        location.setName("서울");

        Position position = new Position();
        position.setName("전체");

        project1.setLocation(location);
        project2.setLocation(location);

        // 주인인 ProjectHeadCount에 Project를 매핑하여 저장해야 함
        ProjectHeadCount projectHeadCount1 = new ProjectHeadCount();
        projectHeadCount1.setPosition(position);
        projectHeadCount1.setProject(project1);
        projectHeadCountRepository.save(projectHeadCount1);

        ProjectHeadCount projectHeadCount2 = new ProjectHeadCount();
        projectHeadCount2.setPosition(position);
        projectHeadCount2.setProject(project2);
        projectHeadCountRepository.save(projectHeadCount2);

        projectService.saveProject(project1);
        projectService.saveProject(project2);
        projectService.saveProject(project3);

        em.flush();
        em.clear();

        List<Project> results = projectRepository.findAllByLocationAndPosition("서울", "전체", Pageable.unpaged());

        Assertions.assertThat(results.size()).isEqualTo(2);

        System.out.println("results = " + results.size());
        System.out.println("results.get(0) = " + results.get(0).getLocation().getName() +
                            " results.get(0) = " + results.get(0).getProjectHeadCounts().get(0).getPosition().getName() +
                            " results.get(0) = " + results.get(0).getId());
        System.out.println("results.get(1) = " + results.get(1).getLocation().getName() +
                            " results.get(1) = " + results.get(1).getProjectHeadCounts().get(0).getPosition().getName() +
                            " results.get(1) = " + results.get(1).getId());
    }

    @Test
    @DisplayName("프로젝트 저장")
    void 프로젝트_저장() {
        Project project = new Project();
        project.setTitle("정환이의 프로젝트");
        project.setDescription("프로젝트 설명");

        //Todo: position까지 저장되는지 테스트 추가하기
        ProjectHeadCount projectHeadCount = new ProjectHeadCount();
        projectHeadCount.setProject(project);
        projectHeadCount.setMaxHeadCount(3);
        projectHeadCount.setNowHeadCount(1);
        project.getProjectHeadCounts().add(projectHeadCount);

        projectService.saveProject(project);

        List<Project> findProjects = projectService.getProjectsByTitle("정환이의 프로젝트");
        assertThat(findProjects.size()).isEqualTo(1);
        System.out.println("findProjects = " + findProjects);
        Project findProject = findProjects.get(0);// 저장에 문제가 있음
        assertThat(findProject.getProjectHeadCounts().get(0).getMaxHeadCount()).isEqualTo(3);
    }

    @Test
    public void findProjectsByRecruitingPosition() {
        Project project1 = new Project();
        Project project2 = new Project();

        // 포지션 생성
        Position java = new Position();
        java.setName("JAVA");
        Position c = new Position();
        c.setName("C");
        Position cpp = new Position();
        cpp.setName("CPP");

        // ProjectHeadCount 생성
        ProjectHeadCount projectHeadCount1 = ProjectHeadCount.builder()
                .position(java)
                .project(project1)
                .build();
        java.addProjectHeadCount(projectHeadCount1);
        project1.addProjectHeadCount(projectHeadCount1);

        ProjectHeadCount projectHeadCount2 = ProjectHeadCount.builder()
                .position(c)
                .project(project1)
                .build();
        c.addProjectHeadCount(projectHeadCount2);
        project1.addProjectHeadCount(projectHeadCount2);

        ProjectHeadCount projectHeadCount3 = ProjectHeadCount.builder()
                .position(java)
                .project(project2)
                .build();
        cpp.addProjectHeadCount(projectHeadCount3);
        project2.addProjectHeadCount(projectHeadCount3);

        // 포지션 저장
        positionRepository.save(cpp);

        // 프로젝트 저장
        projectRepository.save(project1);
        projectRepository.save(project2);

        // 중간 테이블 저장
        projectHeadCountRepository.save(projectHeadCount1);
        projectHeadCountRepository.save(projectHeadCount2);
        projectHeadCountRepository.save(projectHeadCount3);

        Position cppPosition = positionRepository.findByName("CPP")
                .orElseThrow(() -> new IllegalArgumentException("There's no Position"));
        List<Project> result = projectService.findProjectsByRecruitingPosition(cppPosition);
        Assertions.assertThat(result).contains(project2);
    }


}
