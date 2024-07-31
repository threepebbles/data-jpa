package study.datajpa.repository;

public interface NestedClosedProjections {
    String getUsername();

    TeamInfo getTeam();

    // Team 참조 시 left join 발생
    interface TeamInfo {
        String getName();
    }
}
