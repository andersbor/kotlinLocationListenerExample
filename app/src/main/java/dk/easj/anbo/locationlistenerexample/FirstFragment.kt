package dk.easj.anbo.locationlistenerexample

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import dk.easj.anbo.locationlistenerexample.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    // From https://codelabs.developers.google.com/codelabs/while-in-use-locatio
    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you
// should receive updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    // Used only for local storage of the last known location. Usually, this would be saved to your
// database, but because this is a simplified sample without a full database, we only need the
// last location to create a Notification if the user navigates away from the app.
    private var currentLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val locationPermissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
            { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        // Precise location access granted.
                        doIt()
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        // Only approximate location access granted.
                        doIt()
                    }
                    else -> {
                        // No location access granted.
                        Snackbar.make(
                            binding.firstFragment,
                            "Sorry no location for you",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    private fun doIt() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.Builder(1000).build()
        /* locationRequest = LocationRequest.create().apply {
             // Sets the desired interval for active location updates. This interval is inexact. You
             // may not receive updates at all if no location sources are available, or you may
             // receive them less frequently than requested. You may also receive updates more
             // frequently than requested if other applications are requesting location at a more
             // frequent interval.
             //
             // IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of
             // targetSdkVersion) may receive updates less frequently than this interval when the app
             // is no longer in the foreground.
             interval = TimeUnit.SECONDS.toMillis(1000)

             // Sets the fastest rate for active location updates. This interval is exact, and your
             // application will never receive updates more frequently than this value.
             fastestInterval = TimeUnit.SECONDS.toMillis(1000)

             // Sets the maximum time when batched location updates are delivered. Updates may be
             // delivered sooner than this interval.
             maxWaitTime = TimeUnit.MINUTES.toMillis(2)

             priority = LocationRequest.PRIORITY_HIGH_ACCURACY
         }*/

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                // Normally, you want to save a new location to a database. We are simplifying
                // things a bit and just saving it as a local variable, as we only need it again
                // if a Notification is created (when the user navigates away from app).
                currentLocation = locationResult.lastLocation

                val message =
                    "current location\n${currentLocation?.latitude} ${currentLocation?.longitude}"
                binding.textviewFirst.append(message + "\n")
                Log.d("PAPPLE", message)
            }
        }

        // Settings: Allow location
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            binding.textviewFirst.text = "Settings: Allow location"
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}