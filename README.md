# Create: Reclamation ⚙️♻️

**Create: Reclamation** is a standalone, lightweight Create addon for Minecraft 1.21.1 on NeoForge that introduces industrial material recovery and reclaimer progression through 4 machine tiers.

---

## ⚙️ Core Philosophy

The Reclaimer is **not** an "uncrafting table" — it is an industrial material recovery machine. Dismantling manufactured items produces controlled material loss, and recovery is governed by machine tier efficiency, material recoverability, and fair accumulation.

---

## 🏆 Reclaimer Machine Tiers

| Tier | Efficiency | Stress Impact | Min RPM | Speed Multiplier | Key Material / Role |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Mechanical Reclaimer** | **70%** | 8.0 SU | 16 RPM | 1.00× | Andesite / Entry-level recovery |
| **Precision Reclaimer** | **85%** | 12.0 SU | 32 RPM | 1.25× | Brass / Precision dismantled parts |
| **Industrial Reclaimer** | **94%** | 16.0 SU | 32 RPM | 1.50× | Sturdy Sheet / High-throughput factory scale |
| **Advanced Reclaimer** | **98%** | 24.0 SU | 64 RPM | 2.00× | Netherite / Late-game precision recovery |

*Note: No reclaimer ever reaches 100% recovery.*

---

## 🔬 Smart Salvage System

1. **Fair Accumulator Recovery**:
   - Uses stateful fractional probability accumulators per item type (`recoveryAccumulator += effectiveRate`).
   - When the accumulator crosses `1.0`, integer items are guaranteed.
   - Long-term throughput converges strictly to the machine's advertised efficiency without punishing random streaks.
2. **Material Categories**:
   - **EASY** (1.00× base recoverability): Ingots, Nuggets, Sheets, Shafts, Casings, Stone.
   - **STANDARD** (0.90× base recoverability): Andesite Alloy, Gearboxes, Funnels, Chutes, standard manufactured items.
   - **DIFFICULT** (0.75× base recoverability): Precision Mechanisms, Electron Tubes, Controllers, Netherite/Diamond components.
3. **Salvaged Scrap**:
   - Unrecovered material fractions are converted into **Salvaged Scrap** based on lost material, which can be recycled into Iron Nuggets.

---

## 🛡️ Recipe Safety & Loop Protection

- **Authoritative Explicit Recipes**: `create_reclamation:reclaiming` recipes take strict priority.
- **Conservative Dynamic Fallback**: Only simple, unambiguous crafting recipes are automatically deconstructed.
- **Recipe Ambiguity Protection**: Items with multiple conflicting crafting recipes (e.g. Chests from different woods) are rejected unless explicitly defined.
- **Anti-Exploit Protection**:
  - Damaged tools and armor are rejected to prevent infinite durability/repair exploits.
  - Single-ingredient recipes (e.g. 1 Ingot → 9 Nuggets or 1 Log → 4 Planks) are rejected.
  - Circular recipes (`Output == Input`) are rejected.
- **Exclusions**: Sequenced assembly, fluid recipes, and multi-stage transformations require explicit JSON recipes.

---

## 📊 Logistics, Goggles & JEI

- **Engineer's Goggles HUD**: Live in-world display of current target, machine efficiency, expected recovery outputs, scrap possibility, and speed-scaled animated progress.
- **Create Display Link**: Transmit dismantling progress to Nixie Tubes and Flap Displays.
- **JEI Integration**: Category showing recoverable outputs, material categories, tier-by-tier recovery chances, and scrap fallback.
- **Full Automation**: Direct belt input support with Funnels, Chutes, Belts, and Hoppers.

---

## Configuration (`config/create_reclamation-server.toml`)

| Setting | Default | Description |
| :--- | :---: | :--- |
| `mechanicalEfficiency` | `0.70` | Mechanical Reclaimer recovery efficiency (70%). |
| `precisionEfficiency` | `0.85` | Precision Reclaimer recovery efficiency (85%). |
| `industrialEfficiency` | `0.94` | Industrial Reclaimer recovery efficiency (94%). |
| `advancedEfficiency` | `0.98` | Advanced Reclaimer recovery efficiency (98%, capped at 0.98). |
| `scrapConversionRate` | `1.0` | Multiplier for Salvaged Scrap generation on unrecovered fractions. |
| `enableCraftingFallback` | `true` | Enable safe dynamic crafting recipe deconstruction fallback. |

---

## License
MIT License. Created for the Create mod ecosystem.

