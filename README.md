# Create: Reclamation ⚙️♻️

**Create: Reclamation** is a standalone, lightweight Create addon for Minecraft 1.21.1 on NeoForge that introduces industrial deconstruction and material recovery through the **Mechanical Reclaimer**.

---

## Features

- **The Mechanical Reclaimer (`create_reclamation:mechanical_reclaimer`)**:
  - Mechanically dismantles manufactured items, machines, and tools back into their constituent ingredients.
  - Powered by Create kinetics via a rear shaft socket.
  - Stress Impact: **8.0 capacity / RPM** (configurable).
  - Faster rotational speeds (RPM) accelerate dismantling throughput.
  - Fully animated 3D recessed gearbox casing and depot processing plate.

- **Kinetic Audio & Particle VFX**:
  - Mechanical grinding and ratcheting audio dynamically pitch-scaled to machine RPM.
  - Emits item debris and machine sparks while active, followed by satisfying metallic anvil completion cues.

- **Scrap Balancing System (`Salvaged Scrap`)**:
  - Fragile or fractured components broken during violent mechanical deconstruction yield **Salvaged Scrap**.
  - Salvaged Scrap can be recycled in a Furnace or Blast Furnace into **Iron Nuggets**.

- **Create Logistics Integration**:
  - Direct belt input support (`DirectBeltInputBehaviour`) allowing **Create Funnels, Chutes, Belts, and Hoppers** to feed and extract items automatically.

- **Engineer's Goggles In-World HUD**:
  - Wearing Engineer's Goggles reveals live machine status (`Dismantling: [Item]`), real-time animated progress bars (`[======>   ] 65%`), and output buffer diagnostics.

- **Create Display Link Integration**:
  - Attach a **Display Link** to project live dismantling progress bars and percentages directly onto **Nixie Tubes** and **Flap Display Boards**.

- **JEI & Ponder Integration**:
  - Interactive, multi-scene 3D **Ponder** tutorials for basic operation and automated reclamation lines.
  - Full **JEI (Just Enough Items)** category with salvage drop chances and scrap tooltips.

---

## Datapack Custom Recipes (`create_reclamation:reclaiming`)

Datapacks and modpacks can define custom dismantling recipes with individual per-item drop chances:

```json
{
  "type": "create_reclamation:reclaiming",
  "ingredient": {
    "item": "create:andesite_casing"
  },
  "input_count": 1,
  "processing_time": 60,
  "min_speed": 16,
  "results": [
    {
      "item": {
        "id": "create:andesite_alloy",
        "count": 1
      },
      "chance": 0.9
    },
    {
      "item": {
        "id": "minecraft:stripped_oak_log",
        "count": 1
      },
      "chance": 0.85
    }
  ]
}
```

*Note: If no explicit custom recipe is provided, the Mechanical Reclaimer automatically deconstructs any valid crafting recipe dynamically.*

---

## Configuration (`config/create_reclamation-server.toml`)

| Setting | Default | Description |
| :--- | :---: | :--- |
| `stressImpact` | `8.0` | Kinetic stress consumed per RPM. |
| `defaultSalvageRate` | `0.85` | Probability (0.0–1.0) to recover components intact without scrap. |
| `enableCraftingFallback` | `true` | Toggle dynamic crafting recipe deconstruction fallback. |
| `minOperatingSpeed` | `16.0` | Minimum RPM required for the machine to operate. |

---

## Crafting Recipe

| Grid | Ingredients |
| :---: | :--- |
| **Top** | Empty \| `create:brass_hand` \| Empty |
| **Middle** | `create:shaft` \| `create:brass_casing` \| `create:shaft` |
| **Bottom** | Empty \| `create:andesite_alloy` \| Empty |

---

## License
MIT License. Created for the Create mod ecosystem.
