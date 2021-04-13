package fr.maif.workshop.service;

import fr.maif.json.JsonRead;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import static fr.maif.json.JsonRead._string;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@ToString
public class StarWarsCharacter {

    public final String birthYear;
    public final String eyeColor;
    public final String gender;
    public final String hairColor;
    public final String height;
    public final String mass;
    public final String name;

    public static JsonRead<StarWarsCharacter> readCharacter() {
        return _string("birth_year", StarWarsCharacter.builder()::birthYear)
                .and(_string("eye_color"), StarWarsCharacterBuilder::eyeColor)
                .and(_string("gender"), StarWarsCharacterBuilder::gender)
                .and(_string("hair_color"), StarWarsCharacterBuilder::hairColor)
                .and(_string("height"), StarWarsCharacterBuilder::height)
                .and(_string("mass"), StarWarsCharacterBuilder::mass)
                .and(_string("name"), StarWarsCharacterBuilder::name)
                .map(StarWarsCharacterBuilder::build);
    }


}
