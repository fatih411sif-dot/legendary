# Legendary Blades (Fabric, Minecraft 26.3)

Mod senjata legendaris bergaya katana, masing-masing dengan 2 skill.

## Senjata

### Yagura no Tachi
- **Klik kanan - Crimson Wave:** gelombang tebasan sejauh 14 blok (12 damage, cooldown 2,5 detik).
- **Shift + klik kanan - Shadow Step:** melesat 9 blok dan menebas musuh di jalur (10 damage, cooldown 6 detik).
- Resep: Echo Shard di 4 sudut, Netherite Ingot di sisi, Netherite Sword di tengah.
  Atau: `/give @s legendary_blades:yagura_no_tachi`

## Instalasi
1. Pasang Fabric Loader 0.19.5+ dan Java 25.
2. Unduh `legendary_blades-1.0.0.jar` dari halaman **Releases** dan taruh di folder `mods` bersama Fabric API.
3. (Opsional) Unduh `Legendary-Blades-ResourcePack.zip` dan taruh di folder `resourcepacks`.

## Build sendiri
`gradle build` dengan JDK 25. Hasil ada di `build/libs/`.

## Menambah senjata baru
Buat class baru `extends LegendaryBladeItem` (isi `skill1` dan `skill2`), daftarkan di `ModItems`,
lalu tambah client item di `assets/legendary_blades/items/`, model, tekstur, dan teks lang.

## Lisensi
MIT
