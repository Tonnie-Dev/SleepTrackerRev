/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_sleep_tracker.*

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    private lateinit var factory: SleepTrackerViewModelFactory


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        val binding = FragmentSleepTrackerBinding.inflate(inflater)


        //application

        val application = requireNotNull(activity).application


        //Dao

        val dao = SleepDatabase.getInstance(application).sleepDatabaseDao

        //initialize ViewModel Factory


        factory = SleepTrackerViewModelFactory(dao, application)


        val viewModel = ViewModelProvider(this, factory).get(SleepTrackerViewModel::class.java)


        //link binding with the viewModel

        binding.viewModel = viewModel


        //make binding observe liveData

        binding.lifecycleOwner = viewLifecycleOwner


        //observe Nav Event
        viewModel.eventNavigateToSleepDetail.observe(viewLifecycleOwner) { night ->

            val id = night?.nightId

            id?.let {

                findNavController().navigate(
                        SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment(it))
                viewModel.navigationComplete()

            }
        }


        //observe SnackBarEvent

        viewModel.showSnackBarEvent.observe(viewLifecycleOwner){



             snackBarEvent ->

            if (snackBarEvent){

                Snackbar.make(binding.root, resources.getString(R.string.cleared_message), Snackbar
                        .LENGTH_SHORT).show()
                viewModel.onSnackBarEventFinished()
            }


        }


        //instantiate adapter
        val adapter = SleepNightAdapter(SleepNightClickListener {
id -> viewModel.onSleepNightClicked(id)
        })

        //set RecyclerView's Adapter
        binding.sleepList.adapter = adapter

        //create Layout Manager
        val layoutManager = GridLayoutManager(activity,3)

        //span look-up depending on the index
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {

                return when ( position){

                    0 -> 3
                    else -> 1
                }
            }


        }

        binding.sleepList.layoutManager = layoutManager

        // observe and show night when it is not null
        viewModel.nights.observe(viewLifecycleOwner){


            adapter.addHeaderAndSubmitList(it)
        }

        //observe navigateToSleepDataQuality

        viewModel.navigateToSleepDataQuality.observe(viewLifecycleOwner){

            night ->

            night?.let {

                findNavController().navigate(SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepDetailFragment(it))

                viewModel.onSleepDataQualityNavCompleted()
            }
        }


        return binding.root
    }


}
