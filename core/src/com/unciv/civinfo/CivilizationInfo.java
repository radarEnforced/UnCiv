package com.unciv.civinfo;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Predicate;
import com.unciv.game.UnCivGame;
import com.unciv.models.LinqCollection;
import com.unciv.models.gamebasics.Building;
import com.unciv.models.gamebasics.GameBasics;
import com.unciv.models.stats.CivStats;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by LENOVO on 10/18/2017.
 */
public class CivilizationInfo {
    public static CivilizationInfo current(){ return UnCivGame.Current.civInfo; }

    public CivStats civStats = new CivStats();
    public int baseHappiness = 15;
    public String civName = "Babylon";

    public CivilizationTech tech = new CivilizationTech();
    public int turns = 1;

    public LinqCollection<CityInfo> cities = new LinqCollection<CityInfo>();

    public TileMap tileMap = new TileMap(20);

    public int currentCity =0; //index!

    public CivilizationInfo(){
    }


    public CityInfo getCurrentCity() { return cities.get(currentCity); }

    public int turnsToTech(String TechName) {
        return (int) Math.ceil((float)(GameBasics.Technologies.get(TechName).cost - tech.ResearchOfTech(TechName))
                / getStatsForNextTurn().science);
    }

    public void addCity(Vector2 location){
        CityInfo city = new CityInfo(this,location);
        if(cities.size()==1) city.cityBuildings.builtBuildings.add("Palace");
    }

    public CityInfo getCapital(){
        return cities.first(new Predicate<CityInfo>() {
            @Override
            public boolean evaluate(CityInfo arg0) {
                return arg0.cityBuildings.isBuilt("Palace");
            }
        });
    }

    public void nextTurn()//out boolean displayTech)
    {
        CivStats nextTurnStats = getStatsForNextTurn();
        civStats.add(nextTurnStats);
        if(cities.size() > 0) tech.NextTurn((int)nextTurnStats.science);

        for (CityInfo city : cities) city.nextTurn();

        for(TileInfo tile : tileMap.values()) tile.nextTurn();

        turns += 1;
    }

    public CivStats getStatsForNextTurn() {
        CivStats statsForTurn = new CivStats() {{
            happiness = baseHappiness;
        }};
        HashSet<String> LuxuryResources = new HashSet<String>();
        for (CityInfo city : cities) {
            statsForTurn.add(city.getCityStats());
            LuxuryResources.addAll(city.getLuxuryResources());
        }
        statsForTurn.happiness += LuxuryResources.size() * 5; // 5 happiness for each unique luxury in civ

        return statsForTurn;
    }

    public LinqCollection<String> getCivTags(){
        return cities.selectMany(new LinqCollection.Func<CityInfo, Collection<? extends String>>() {
            @Override
            public Collection<? extends String> GetBy(CityInfo arg0) {
                return arg0.cityBuildings.getBuiltBuildings().select(new LinqCollection.Func<Building, String>() {
                    @Override
                    public String GetBy(Building arg0) {
                        return arg0.unique;
                    }
                });
            }
        });
    }
}
