package com.ssquadteam.fallout.models;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a radiation source in the world
 */
@SerializableAs("RadiationSource")
public class RadiationSource implements ConfigurationSerializable {

    private UUID id;
    private String name;
    private Location location;
    private int radius;
    private int strength;
    private int power;
    private boolean active;

    /**
     * Create a new radiation source
     * 
     * @param name The name of the radiation source
     * @param location The location of the radiation source
     * @param radius The radius of effect in blocks
     * @param strength The strength of radiation at center (0-100)
     * @param power How quickly radiation builds up (1-10)
     */
    public RadiationSource(String name, Location location, int radius, int strength, int power) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location;
        this.radius = Math.max(1, radius);
        this.strength = Math.min(100, Math.max(1, strength));
        this.power = Math.min(10, Math.max(1, power));
        this.active = true;
    }

    /**
     * Create a radiation source from serialized data
     * 
     * @param map The serialized data
     */
    @SuppressWarnings("unchecked")
    public RadiationSource(Map<String, Object> map) {
        this.id = UUID.fromString((String) map.get("id"));
        this.name = (String) map.get("name");
        this.location = (Location) map.get("location");
        this.radius = (int) map.get("radius");
        this.strength = (int) map.get("strength");
        this.power = (int) map.get("power");
        this.active = (boolean) map.get("active");
    }

    /**
     * Serialize the radiation source to a map
     * 
     * @return The serialized data
     */
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id.toString());
        map.put("name", name);
        map.put("location", location);
        map.put("radius", radius);
        map.put("strength", strength);
        map.put("power", power);
        map.put("active", active);
        return map;
    }

    /**
     * Calculate the radiation level at a specific location
     * 
     * @param target The location to check
     * @return Radiation level (0-100)
     */
    public int getRadiationLevelAt(Location target) {
        if (!active || !target.getWorld().equals(location.getWorld())) {
            return 0;
        }

        double distance = location.distance(target);
        
        // If outside radius, no radiation
        if (distance > radius) {
            return 0;
        }
        
        // Linear falloff based on distance
        double distanceRatio = 1.0 - (distance / radius);
        int radiationLevel = (int) (strength * distanceRatio);
        
        // Apply power factor (affects how quickly radiation builds up)
        double powerFactor = power / 5.0; // Power 5 = 1.0x multiplier
        radiationLevel = (int) (radiationLevel * powerFactor);
        
        return Math.min(100, Math.max(0, radiationLevel));
    }

    // Getters and setters
    
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = Math.max(1, radius);
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = Math.min(100, Math.max(1, strength));
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = Math.min(10, Math.max(1, power));
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RadiationSource that = (RadiationSource) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RadiationSource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location=" + location +
                ", radius=" + radius +
                ", strength=" + strength +
                ", power=" + power +
                ", active=" + active +
                '}';
    }
} 