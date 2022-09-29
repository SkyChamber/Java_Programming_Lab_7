package serverStuff;

import mainPart.Chapter;
import mainPart.Coordinates;
import mainPart.Enum_explainer;
import mainPart.SpaceMarine;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.TreeSet;

public class ReadCollection {
    public TreeSet<SpaceMarine> read(Statement st) throws SQLException {
        TreeSet<SpaceMarine> tempset = new TreeSet<>();
        ResultSet resultSet = st.executeQuery("SELECT * FROM ultramarines");
        ResultSet rs = resultSet;
        while (rs.next()) {
            long id = rs.getLong("id");
            Date date = rs.getDate("creation_date");
            SpaceMarine spaceMarine = new SpaceMarine(id, date);
            spaceMarine.setName(rs.getString("name"));
            spaceMarine.setHealth(rs.getInt("hp"));
            Enum_explainer enumExplainer = new Enum_explainer(rs.getString("astartes"), rs.getString("weapon"), rs.getString("mele_weapon"));
            spaceMarine.setCategory(enumExplainer.getAstsrtes());
            spaceMarine.setWeaponType(enumExplainer.getWeapon());
            spaceMarine.setMeleeWeapon(enumExplainer.getMele());
            Coordinates coordinates = new Coordinates(rs.getDouble("x_cord"), rs.getDouble("y_cord"));
            spaceMarine.setCoordinates(coordinates);
            Chapter chapter = new Chapter(rs.getString("chapter_name"), rs.getString("world_name"));
            spaceMarine.setChapter(chapter);

            tempset.add(spaceMarine);
        }
        return tempset;
    }
}
