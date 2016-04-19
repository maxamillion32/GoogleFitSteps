    package com.example.sheyda.googlefitsteps;

    import android.content.Intent;
    import android.content.IntentSender;
    import android.os.Bundle;
    import android.support.v7.app.AppCompatActivity;
    import android.util.Log;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.widget.Toast;

    import com.example.sheyda.fuckk.R;
    import com.google.android.gms.common.ConnectionResult;
    import com.google.android.gms.common.Scopes;
    import com.google.android.gms.common.api.GoogleApiClient;
    import com.google.android.gms.common.api.Scope;
    import com.google.android.gms.common.api.Status;
    import com.google.android.gms.fitness.Fitness;
    import com.google.android.gms.fitness.data.DataPoint;
    import com.google.android.gms.fitness.data.DataSource;
    import com.google.android.gms.fitness.data.DataType;
    import com.google.android.gms.fitness.data.Field;
    import com.google.android.gms.fitness.data.Value;
    import com.google.android.gms.fitness.request.DataSourcesRequest;
    import com.google.android.gms.fitness.request.OnDataPointListener;
    import com.google.android.gms.fitness.request.SensorRequest;
    import com.google.android.gms.fitness.result.DataSourcesResult;
    import com.google.android.gms.common.api.ResultCallback;

    import java.util.concurrent.TimeUnit;

    public class MainActivity extends AppCompatActivity  implements OnDataPointListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

        private static final int REQUEST_OAUTH = 1;
        private static final String AUTH_PENDING = "auth_state_pending";
        private boolean authInProgress = false;
        private GoogleApiClient mApiClient;
        private OnDataPointListener mListener;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            Toast.makeText(getApplicationContext(), "#1, Oncreate, Part 1", Toast.LENGTH_SHORT).show();
            if (savedInstanceState != null) {
                authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
            }

            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Fitness.SENSORS_API)
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            Toast.makeText(getApplicationContext(), "#2, Oncreate, Done", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onStart() {
            super.onStart();
            mApiClient.connect();
            Toast.makeText(getApplicationContext(), "#3, Started", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if( requestCode == REQUEST_OAUTH ) {
                authInProgress = false;
                if( resultCode == RESULT_OK ) {
                    if( !mApiClient.isConnecting() && !mApiClient.isConnected() ) {
                        mApiClient.connect();
                    }
                } else if( resultCode == RESULT_CANCELED ) {
                    Log.e( "GoogleFit", "RESULT_CANCELED" );
                }
            } else {
                Log.e("GoogleFit", "requestCode NOT request_oauth");
            }
        }

        private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
            Toast.makeText(getApplicationContext(), "#8, calling listener", Toast.LENGTH_SHORT).show();

            /*mListener = new OnDataPointListener() {
                @Override
                public void onDataPoint(DataPoint dataPoint) {
                    Toast.makeText(getApplicationContext(), "On Data Point Starting", Toast.LENGTH_SHORT).show();
                    for( final Field field : dataPoint.getDataType().getFields() ) {
                        final Value value = dataPoint.getValue(field);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Field: " + field.getName() + " Value: " + value, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            };
*/
            mListener = new OnDataPointListener() {

                @Override
                public void onDataPoint(DataPoint dataPoint) {
                    Toast.makeText(getApplicationContext(), "On Data Point Starting", Toast.LENGTH_SHORT).show();
                    for (Field field : dataPoint.getDataType().getFields()) {
                        Value value = dataPoint.getValue(field);
                        Toast.makeText(getApplicationContext(), "Field: " + field.getName() + " Value: " + value, Toast.LENGTH_SHORT).show();
                    }
                }

            };
            Toast.makeText(getApplicationContext(), "#9, just after ondatapoint", Toast.LENGTH_SHORT).show();
            SensorRequest request = new SensorRequest.Builder()
                    .setDataSource( dataSource )
                    .setDataType( dataType )
                    .setSamplingRate( 3, TimeUnit.SECONDS )
                    .build();

            /*Fitness.SensorsApi.add(mApiClient, request, mListener)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.e("GoogleFit", "SensorApi successfully added");
                            }
                        }
                    });
*/
            Fitness.SensorsApi.add(
                    mApiClient,
                    request,
                    mListener)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Toast.makeText(getApplicationContext(), "SUCCESSSSS", Toast.LENGTH_SHORT).show();
                                Log.e("GoogleFit", "SensorApi successfully added");
                            } else {
                                Toast.makeText(getApplicationContext(), "FAILLLL", Toast.LENGTH_SHORT).show();
                                Log.e("GoogleFit", "SensorApi not added");
                            }
                        }
                    });
            Toast.makeText(getApplicationContext(), "#10, Sensor API Added", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onConnected(Bundle bundle) {
            DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                    .setDataTypes( DataType.TYPE_STEP_COUNT_CUMULATIVE )
                    .setDataSourceTypes( DataSource.TYPE_DERIVED )
                    .build();
            Toast.makeText(getApplicationContext(), "#4, OnConnected Called", Toast.LENGTH_SHORT).show();
            ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
                @Override
                public void onResult(DataSourcesResult dataSourcesResult) {
                    Toast.makeText(getApplicationContext(), "#6, ON RESULT OVERRIDE", Toast.LENGTH_SHORT).show();
                    for( DataSource dataSource : dataSourcesResult.getDataSources() ) {
                        if( DataType.TYPE_STEP_COUNT_CUMULATIVE.equals( dataSource.getDataType() ) ) {
                            Toast.makeText(getApplicationContext(), "#7, REGISTERING!", Toast.LENGTH_SHORT).show();
                            registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);
                        }
                    }
                }
            };

            Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest)
                    .setResultCallback(dataSourcesResultCallback);
            Toast.makeText(getApplicationContext(), "#5, Getting Sensor Data " + dataSourcesResultCallback.toString(), Toast.LENGTH_SHORT).show();
            //Toast.makeText(getApplicationContext(), "Getting Sensor Data " + dataSourcesResultCallback.onResult(), Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            if( !authInProgress ) {
                try {
                    authInProgress = true;
                    connectionResult.startResolutionForResult( MainActivity.this, REQUEST_OAUTH );
                } catch(IntentSender.SendIntentException e ) {

                }
            } else {
                Log.e("GoogleFit", "authInProgress");
            }
        }

        @Override
        public void onDataPoint(DataPoint dataPoint) {

        }

        @Override
        protected void onStop() {
            super.onStop();

            Fitness.SensorsApi.remove( mApiClient, this )
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                mApiClient.disconnect();
                            }
                        }
                    });
        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean(AUTH_PENDING, authInProgress);
        }


    }
