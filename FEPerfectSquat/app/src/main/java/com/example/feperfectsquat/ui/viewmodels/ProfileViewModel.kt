package com.example.feperfectsquat.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.feperfectsquat.data.ProfileRepository
import com.example.feperfectsquat.models.ProfileDraft
import com.example.feperfectsquat.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** Onboarding profile: the form draft and the saved profile that unlocks the app. */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProfileRepository(application)

    private val _profile = MutableStateFlow(repository.load())
    val profile: StateFlow<UserProfile?> = _profile

    private val _draft = MutableStateFlow(ProfileDraft())
    val draft: StateFlow<ProfileDraft> = _draft

    fun updateDraft(change: (ProfileDraft) -> ProfileDraft) = _draft.update(change)

    /** Saves the draft; returns false when the form is still incomplete. */
    fun saveDraft(): Boolean {
        val profile = _draft.value.toProfile() ?: return false
        repository.save(profile)
        _profile.value = profile
        return true
    }
}
