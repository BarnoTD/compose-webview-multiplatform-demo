// ==================== API SECTION ====================
// This section contains all API functions using native bridge

const API = {
    // Get doctor information
    getDoctor: function() {
        console.log('getDoctor called');
        
        return new Promise((resolve, reject) => {
            window.kmpJsBridge.callNative(
                "getDoctor",
                JSON.stringify({}),
                function(result) {
                    try {
                        const doctorData = JSON.parse(result);
                        console.log('Doctor data returned:', doctorData);
                        resolve(doctorData);
                    } catch (error) {
                        console.error('Error parsing doctor data:', error);
                        reject(error);
                    }
                }
            );
        });
    },

    // Initialize with doctor data
    setDoctor: function(doctorData) {
        console.log('setDoctor called with:', doctorData);
        
        return new Promise((resolve, reject) => {
            window.kmpJsBridge.callNative(
                "setDoctor",
                JSON.stringify(doctorData),
                function(result) {
                    try {
                        const response = JSON.parse(result);
                        resolve(response);
                    } catch (error) {
                        console.error('Error in setDoctor:', error);
                        reject(error);
                    }
                }
            );
        });
    },

    // Select date range and get appointments
    selectDate: function(from, to) {
        console.log('selectDate called with:', { from, to });
        
        return new Promise((resolve, reject) => {
            window.kmpJsBridge.callNative(
                "selectDate",
                JSON.stringify({ from: from, to: to }),
                function(result) {
                    try {
                        const appointments = JSON.parse(result);
                        console.log('Appointments returned:', appointments);
                        resolve(appointments);
                    } catch (error) {
                        console.error('Error parsing appointments:', error);
                        reject(error);
                    }
                }
            );
        });
    },

    // Search patients
    queryPatient: function(query) {
        console.log('queryPatient called with:', query);
        
        return new Promise((resolve, reject) => {
            window.kmpJsBridge.callNative(
                "queryPatient",
                JSON.stringify({ query: query }),
                function(result) {
                    try {
                        const patients = JSON.parse(result);
                        console.log('Patients returned:', patients);
                        resolve(patients);
                    } catch (error) {
                        console.error('Error parsing patients:', error);
                        reject(error);
                    }
                }
            );
        });
    },

    // Save appointment
    saveAppointment: function(appointmentData) {
        console.log('saveAppointment called with:', appointmentData);
        
        return new Promise((resolve, reject) => {
            window.kmpJsBridge.callNative(
                "saveAppointment",
                JSON.stringify(appointmentData),
                function(result) {
                    try {
                        const response = JSON.parse(result);
                        console.log('Appointment saved:', response);
                        resolve(response);
                    } catch (error) {
                        console.error('Error saving appointment:', error);
                        reject(error);
                    }
                }
            );
        });
    },

    // Delete appointment
    deleteAppointment: function(appointmentId) {
        console.log('deleteAppointment called with:', appointmentId);
        
        return new Promise((resolve, reject) => {
            window.kmpJsBridge.callNative(
                "deleteAppointment",
                JSON.stringify({ appointmentId: appointmentId }),
                function(result) {
                    try {
                        const response = JSON.parse(result);
                        console.log('Appointment deleted:', response);
                        resolve(response);
                    } catch (error) {
                        console.error('Error deleting appointment:', error);
                        reject(error);
                    }
                }
            );
        });
    },

    // Edit appointment
    editAppointment: function(appointmentData) {
        console.log('editAppointment called with:', appointmentData);
        
        return new Promise((resolve, reject) => {
            window.kmpJsBridge.callNative(
                "editAppointment",
                JSON.stringify(appointmentData),
                function(result) {
                    try {
                        const response = JSON.parse(result);
                        console.log('Appointment updated:', response);
                        resolve(response);
                    } catch (error) {
                        console.error('Error editing appointment:', error);
                        reject(error);
                    }
                }
            );
        });
    }
};

