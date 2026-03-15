package dao;

import config.DBConfig;
import entity.Discount;
import entity.filter.DiscountFilter;
import enums.DiscountStatusEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DiscountDAO implements GenericDAO<Discount, DiscountFilter>{

    private DBConfig dbConfig = new DBConfig();

    @Override
    public List<Discount> findAll(DiscountFilter filter) {

        List<Discount> list = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT d.*
            FROM discount d
            WHERE d.deleted_at IS NULL
        """);

        if(filter != null){

            if(filter.getSearch() != null && !filter.getSearch().isBlank()){

                sql.append("""
                    AND (
                        LOWER(d.code) LIKE ?
                    )
                """);

                String keyword = "%" + filter.getSearch().trim().toLowerCase() + "%";
                params.add(keyword);
            }

            if(filter.getStatus() != null){

                sql.append(" AND d.status = ? ");
                params.add(filter.getStatus().getValue());
            }

            if(filter.getFromStartedAt() != null){

                sql.append(" AND d.started_at >= ? ");
                params.add(Timestamp.valueOf(filter.getFromStartedAt()));
            }

            if(filter.getToEndedAt() != null){

                sql.append(" AND d.ended_at <= ? ");
                params.add(Timestamp.valueOf(filter.getToEndedAt()));
            }
        }

        try(
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString());
        ){

            for(int i = 0; i < params.size(); i++){
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                list.add(mapRow(rs));
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Discount findById(int id) {

        String sql = """
            SELECT *
            FROM discount
            WHERE id = ?
            AND deleted_at IS NULL
        """;

        try(
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ){

            ps.setInt(1,id);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                return mapRow(rs);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean insert(Discount request) {

        String sql = """
            INSERT INTO discount
            (code, discount_type, discount_value, maximum_discount,
             started_at, ended_at, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE())
        """;

        try(
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ){

            ps.setString(1, request.getCode());
            ps.setString(2, request.getDiscountType());
            ps.setInt(3, request.getDiscountValue());
            ps.setInt(4, request.getMaximumDiscount());
            ps.setTimestamp(5, Timestamp.valueOf(request.getStartedAt()));
            ps.setTimestamp(6, Timestamp.valueOf(request.getEndedAt()));
            ps.setInt(7, request.getStatus().getValue());

            return ps.executeUpdate() > 0;

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean update(Discount request) {

        String sql = """
            UPDATE discount
            SET
                code = ?,
                discount_type = ?,
                discount_value = ?,
                maximum_discount = ?,
                started_at = ?,
                ended_at = ?,
                status = ?,
                updated_at = GETDATE()
            WHERE id = ?
        """;

        try(
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ){

            ps.setString(1, request.getCode());
            ps.setString(2, request.getDiscountType());
            ps.setInt(3, request.getDiscountValue());
            ps.setInt(4, request.getMaximumDiscount());
            ps.setTimestamp(5, Timestamp.valueOf(request.getStartedAt()));
            ps.setTimestamp(6, Timestamp.valueOf(request.getEndedAt()));
            ps.setInt(7, request.getStatus().getValue());
            ps.setInt(8, request.getId());

            return ps.executeUpdate() > 0;

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean delete(int id) {

        String sql = """
            UPDATE discount
            SET deleted_at = GETDATE()
            WHERE id = ?
        """;

        try(
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ){

            ps.setInt(1,id);

            return ps.executeUpdate() > 0;

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    private Discount mapRow(ResultSet rs) throws Exception{

        Discount d = new Discount();

        d.setId(rs.getInt("id"));
        d.setCode(rs.getString("code"));
        d.setDiscountType(rs.getString("discount_type"));
        d.setDiscountValue(rs.getInt("discount_value"));
        d.setMaximumDiscount(rs.getInt("maximum_discount"));

        Timestamp started = rs.getTimestamp("started_at");
        Timestamp ended = rs.getTimestamp("ended_at");

        if(started != null) d.setStartedAt(started.toLocalDateTime());
        if(ended != null) d.setEndedAt(ended.toLocalDateTime());

        d.setStatus(DiscountStatusEnum.fromValue(rs.getInt("status")));

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        if(created != null) d.setCreatedAt(created.toLocalDateTime());
        if(updated != null) d.setUpdatedAt(updated.toLocalDateTime());

        return d;
    }
}