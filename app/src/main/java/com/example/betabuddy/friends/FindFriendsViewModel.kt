//package com.example.betabuddy.friends
//
//import androidx.lifecycle.*
//import com.example.betabuddy.data.UsersRepository
//import com.example.betabuddy.model.UserProfile
//import kotlinx.coroutines.launch
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.map
//
//
//class FindFriendsViewModel(
//    private val repo: UsersRepository
//) : ViewModel() {
//
//    /** Raw results as full models (use this once you switch to a typed adapter). */
//    val results: LiveData<List<UserProfile>> = repo.results
//
//    /** Convenience: map to display strings to work with your current SimpleResultsAdapter. */
//    val resultRows: LiveData<List<String>> =
//        Transformations.map(results) { list ->
//            list.map { u ->
//                val name = u.name.ifBlank { "Anonymous" }
//                val role = u.skillLevel.ifBlank { "Climber" }
//                val loc  = u.location.ifBlank { "Anywhere" }
//                "$name ($role) — $loc"
//            }
//        }
//
//    /** Trigger a search; pass null or blank to fetch all users. */
//    fun search(location: String?) = viewModelScope.launch {
//        val filter = location?.trim().takeUnless { it.isNullOrEmpty() }
//        repo.search(filter)
//    }
//}
//
///** Factory so you can pass a repository in from your ServiceLocator (or Hilt). */
//class FindFriendsVMFactory(
//    private val repo: FriendRepository
//) : ViewModelProvider.Factory {
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        require(modelClass.isAssignableFrom(FindFriendsViewModel::class.java))
//        return FindFriendsViewModel(repo) as T
//    }
//}
