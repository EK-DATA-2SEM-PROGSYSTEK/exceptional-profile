package ek.ianb.exceptionalprofile.repository;
import ek.ianb.exceptionalprofile.model.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ProfileRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Profile> profileRowMapper = (rs, rowNum) ->
            new Profile(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email")
            );

    public ProfileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Profile> findAll() {
        String sql = """
                SELECT id, name, email
                FROM profiles
                ORDER BY id
                """;
        return jdbcTemplate.query(sql, profileRowMapper);
    }

    public Profile findById(int id) {
        String sql = """
                SELECT id, name, email
                FROM profiles
                WHERE id = ?
                """;

        List<Profile> results = jdbcTemplate.query(sql, profileRowMapper, id);
        if (results.isEmpty()) {
            return null;
        }
        return results.getFirst();
    }

    public Profile insert(Profile profile) {
        String sql = """
                INSERT INTO profiles (name, email)
                VALUES (?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, profile.getName());
            ps.setString(2, profile.getEmail());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to retrieve generated id.");
        }

        return new Profile(key.intValue(), profile.getName(), profile.getEmail());
    }

    public boolean update(Profile profile) {
        String sql = """
            UPDATE profiles
            SET name = ?, email = ?
            WHERE id = ?
            """;

        int rowsUpdated = jdbcTemplate.update(
                sql,
                profile.getName(),
                profile.getEmail(),
                profile.getId()
        );

        return rowsUpdated > 0;
    }

    public boolean deleteById(int id) {
        String sql = """
            DELETE FROM profiles
            WHERE id = ?
            """;
        int rowsDeleted = jdbcTemplate.update(sql, id);
        return rowsDeleted > 0;
    }
}
