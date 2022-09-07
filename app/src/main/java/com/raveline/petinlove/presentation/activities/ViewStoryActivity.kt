package com.raveline.petinlove.presentation.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.raveline.petinlove.R
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.data.model.hashMapToUserModel
import com.raveline.petinlove.databinding.ActivityViewStoryBinding
import com.raveline.petinlove.domain.utils.SystemFunctions
import com.raveline.petinlove.presentation.viewmodels.StoryViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.StoryViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ViewStoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {

    private var counter = 0
    private var pressTime = 0L
    private var limit = 500L

    @Inject
    lateinit var storyViewModelFactory: StoryViewModelFactory
    private val storyViewModel: StoryViewModel by viewModels { storyViewModelFactory }

    private var _binding: ActivityViewStoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var circular: CircularProgressDrawable

    private val args by navArgs<ViewStoryActivityArgs>()

    private lateinit var myUser: UserModel

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = View.OnTouchListener { _, motionEvent ->

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                binding.storiesProgressViewStoryActivity.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                binding.storiesProgressViewStoryActivity.resume()
                return@OnTouchListener limit < (now - pressTime)
            }
        }
        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_view_story)
        setContentView(binding.root)
        initView()
        initObservers()

    }

    private fun initView() {
        storyViewModel.getStoriesImages(
            args.userId
        )

        myUser = SystemFunctions.getLoggedUserFromPref(this)!!
        if (myUser.uid == args.userId) {
            binding.apply {
                imageViewStoryActivityDeleteViews.visibility = VISIBLE
            }
        }

        circular = CircularProgressDrawable(applicationContext).apply {
            strokeWidth = 10f
            centerRadius = 50f
            start()
        }

        binding.apply {
            backStoryActivity.setOnClickListener {
                storiesProgressViewStoryActivity.reverse()
            }
            backStoryActivity.setOnTouchListener(onTouchListener)

            nextStoryActivity.setOnClickListener {
                storiesProgressViewStoryActivity.skip()
            }
            nextStoryActivity.setOnTouchListener(onTouchListener)

            imageViewStoryActivityDeleteViews.setOnClickListener {
                storyViewModel.deleteStory(
                    args.userId,
                    storyViewModel.storyFlow.value[counter].storyId,
                    binding.root
                )
            }
        }
    }

    private fun initObservers() {
        lifecycleScope.launch {
            storyViewModel.imagesStateFlow.collectLatest { images ->
                if (images.isNotEmpty()) {
                    displayStories(images)
                }

            }
        }
        displayUserData(userId = args.userId)
    }

    private fun displayStories(images: List<String>) {
        binding.storiesProgressViewStoryActivity.apply {
            setStoriesCount(images.size)
            setStoryDuration(5000L)
            setStoriesListener(this@ViewStoryActivity)
            startStories(counter)
        }
        Glide.with(baseContext)
            .load(images[counter])
            .placeholder(circular)
            .centerCrop()
            .into(binding.imageViewStoryActivity)
    }

    private fun displayUserData(userId: String) {
        lifecycleScope.launch {
            storyViewModel.getUserById(userId).get().addOnSuccessListener { doc ->
                if (doc != null) {
                    val user = hashMapToUserModel(doc)

                    Glide.with(baseContext)
                        .load(user.userProfileImage)
                        .placeholder(circular)
                        .centerCrop()
                        .into(binding.circleImageViewStoryActivity)
                    binding.textViewStoryActivity.text = user.userName
                }
            }
        }
    }

    override fun onNext() {
        Glide.with(baseContext)
            .load(storyViewModel.imagesStateFlow.value[++counter])
            .placeholder(circular)
            .centerCrop()
            .into(binding.imageViewStoryActivity)

    }

    override fun onPrev() {
        if ((counter - 1) < 0) return
        Glide.with(baseContext)
            .load(storyViewModel.imagesStateFlow.value[++counter])
            .placeholder(circular)
            .centerCrop()
            .into(binding.imageViewStoryActivity)

    }

    override fun onComplete() {
        finish()
    }

    override fun onPause() {
        binding.storiesProgressViewStoryActivity.pause()
        super.onPause()
    }

    override fun onRestart() {
        binding.storiesProgressViewStoryActivity.resume()
        super.onRestart()
    }

    override fun onDestroy() {
        binding.storiesProgressViewStoryActivity.destroy()
        _binding = null
        super.onDestroy()
    }
}