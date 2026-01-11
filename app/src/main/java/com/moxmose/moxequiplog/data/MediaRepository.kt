package com.moxmose.moxequiplog.data

import com.moxmose.moxequiplog.data.local.*
import com.moxmose.moxequiplog.ui.options.EquipmentIconProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MediaRepository(
    private val mediaDao: MediaDao,
    private val categoryDao: CategoryDao,
    private val appColorDao: AppColorDao
) {
    
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allMedia: Flow<List<Media>> = mediaDao.getAllMedia()
    val allColors: Flow<List<AppColor>> = appColorDao.getAllColors()

    fun getMediaByCategory(category: String): Flow<List<Media>> = mediaDao.getMediaByCategory(category)

    suspend fun initializeAppData() {
        // 1. Inizializza Colori
        val existingColors = appColorDao.getAllColors().first()
        if (existingColors.isEmpty()) {
            val defaultColors = mutableListOf<AppColor>()
            var order = 0
            val standard = listOf("#4CAF50" to "Verde", "#2196F3" to "Blu", "#F44336" to "Rosso")
            standard.forEach { (hex, name) -> defaultColors.add(AppColor(hex, name, true, order++)) }
            
            val vivid = listOf("#883932" to "C64 Red", "#67B6BD" to "C64 Cyan", "#55AA3C" to "C64 Green")
            vivid.forEach { (hex, name) -> defaultColors.add(AppColor(hex, name, true, order++)) }

            val pastel = listOf("#FFD1DC" to "Pastel Pink", "#AEC6CF" to "Pastel Blue", "#77DD77" to "Pastel Green")
            pastel.forEach { (hex, name) -> defaultColors.add(AppColor(hex, name, true, order++)) }

            defaultColors.forEach { appColorDao.insertColor(it) }
        }

        // 2. Inizializza Categorie
        val existingCategories = categoryDao.getAllCategories().first()
        if (existingCategories.isEmpty()) {
            categoryDao.insertCategory(Category("EQUIPMENT", "Equipaggiamento", "#4CAF50"))
            categoryDao.insertCategory(Category("OPERATION", "Operazioni", "#2196F3"))
        }

        // 3. Inizializza Icone
        categoryDao.getAllCategories().first().forEach { category ->
            initializeIconsForCategory(category.id)
        }
    }

    private suspend fun initializeIconsForCategory(categoryId: String) {
        val currentMedia = mediaDao.getMediaByCategory(categoryId).first()
        val existingUris = currentMedia.map { it.uri }
        if (!existingUris.contains("icon:none")) {
            val maxOrder = mediaDao.getMaxOrder(categoryId) ?: -1
            mediaDao.insertMedia(Media(uri = "icon:none", category = categoryId, mediaType = "ICON", displayOrder = maxOrder + 1))
        }
        EquipmentIconProvider.icons.keys.forEach { iconId ->
            val uri = "icon:$iconId"
            if (!existingUris.contains(uri)) {
                val maxOrder = mediaDao.getMaxOrder(categoryId) ?: -1
                mediaDao.insertMedia(Media(uri = uri, category = categoryId, mediaType = "ICON", displayOrder = maxOrder + 1))
            }
        }
    }

    suspend fun addMedia(uri: String, category: String) {
        val maxOrder = mediaDao.getMaxOrder(category) ?: -1
        mediaDao.insertMedia(Media(uri = uri, category = category, mediaType = "IMAGE", displayOrder = maxOrder + 1))
    }

    suspend fun updateMediaOrder(mediaList: List<Media>) {
        mediaDao.updateAllMedia(mediaList)
    }

    suspend fun removeMedia(uri: String, category: String) {
        val media = mediaDao.getMediaByUriAndCategory(uri, category)
        if (media != null && media.mediaType == "IMAGE") {
            mediaDao.deleteMedia(media)
        }
    }

    suspend fun setCategoryDefault(categoryId: String, iconId: String?, photoUri: String?) {
        val category = categoryDao.getCategoryById(categoryId)
        if (category != null) {
            categoryDao.insertCategory(category.copy(
                defaultIconIdentifier = iconId,
                defaultPhotoUri = photoUri
            ))
        }
    }

    suspend fun updateCategoryColor(categoryId: String, colorHex: String) {
        val category = categoryDao.getCategoryById(categoryId)
        if (category != null) {
            categoryDao.insertCategory(category.copy(color = colorHex))
        }
    }

    suspend fun addColor(hex: String, name: String) {
        val maxOrder = appColorDao.getMaxOrder() ?: -1
        appColorDao.insertColor(AppColor(hex, name, false, maxOrder + 1))
    }

    suspend fun deleteColor(color: AppColor) {
        appColorDao.deleteColor(color)
    }

    suspend fun updateColorsOrder(colors: List<AppColor>) {
        appColorDao.updateAllColors(colors)
    }
}
