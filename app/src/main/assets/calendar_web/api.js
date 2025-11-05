// ==================== API SECTION ====================
// This section contains all API functions and dummy data

// Dummy data for patients
const dummyPatients = [
    {
        "id": "1",
        "name": "John Smith",
        "email": "john.smith@email.com",
        "phone": "+1234567890",
        "date_of_birth": "1980-01-15",
        "address": "123 Main St, City, State"
    },
    {
        "id": "2",
        "name": "Sarah Johnson",
        "email": "sarah.j@email.com",
        "phone": "+1234567891",
        "date_of_birth": "1985-05-22",
        "address": "456 Oak Ave, City, State"
    },
    {
        "id": "3",
        "name": "Michael Brown",
        "email": "m.brown@email.com",
        "phone": "+1234567892",
        "date_of_birth": "1975-11-08",
        "address": "789 Pine Rd, City, State"
    },
    {
        "id": "4",
        "name": "Emily Davis",
        "email": "emily.d@email.com",
        "phone": "+1234567893",
        "date_of_birth": "1990-03-18",
        "address": "321 Elm St, City, State"
    },
    {
        "id": "5",
        "name": "David Wilson",
        "email": "d.wilson@email.com",
        "phone": "+1234567894",
        "date_of_birth": "1982-07-25",
        "address": "654 Maple Dr, City, State"
    },
    {
        "id": "6",
        "name": "Lisa Anderson",
        "email": "lisa.a@email.com",
        "phone": "+1234567895",
        "date_of_birth": "1988-09-12",
        "address": "987 Cedar Ln, City, State"
    }
];

// Dummy appointments data
let dummyAppointments = [
    {
        "id": "1001",
        "title": "John Smith",
        "notes": "Regular checkup",
        "from": Math.floor(new Date().setHours(9, 0, 0, 0) / 1000),
        "duration_minutes": 60,
        "patient_id": "1",
        "type": "Patient"
    },
    {
        "id": "1002",
        "title": "Sarah Johnson",
        "notes": "Follow-up consultation",
        "from": Math.floor(new Date().setHours(11, 30, 0, 0) / 1000),
        "duration_minutes": 30,
        "patient_id": "2",
        "type": "Patient"
    },
    {
        "id": "1003",
        "title": "Team Meeting",
        "notes": "Weekly team sync",
        "from": Math.floor(new Date().setHours(14, 0, 0, 0) / 1000),
        "duration_minutes": 45,
        "patient_id": "",
        "type": "Other"
    }
];

// API Functions
const API = {
    // Get doctor information
    getDoctor: function() {
        console.log('getDoctor called');
        
        return new Promise((resolve) => {
            setTimeout(() => {
                const doctorData = {
                    "id": "doc001",
                    "name": "Dr. Joe Smith",
                    "specialization": "General Practice",
                    "email": "dr.joe@hospital.com",
                    "phone": "+1234567890"
                };
                
                console.log('Doctor data returned:', doctorData);
                resolve(doctorData);
            }, 300);
        });
    },

    // Initialize with doctor data
    setDoctor: function(doctorData) {
        console.log('setDoctor called with:', doctorData);
        // In real app, this would initialize the doctor context
        return Promise.resolve({ success: true });
    },

    // Select date range and get appointments
    selectDate: function(from, to) {
        console.log('selectDate called with:', { from, to });
        
        // Simulate API delay
        return new Promise((resolve) => {
            setTimeout(() => {
                // Filter appointments within the date range
                const filteredAppointments = dummyAppointments.filter(apt => {
                    return apt.from >= from && apt.from < to;
                });
                
                console.log('Appointments returned:', filteredAppointments);
                resolve(filteredAppointments);
            }, 300);
        });
    },

    // Search patients
    queryPatient: function(query) {
        console.log('queryPatient called with:', query);
        
        return new Promise((resolve) => {
            setTimeout(() => {
                const filteredPatients = dummyPatients.filter(patient => 
                    patient.name.toLowerCase().includes(query.toLowerCase()) ||
                    patient.email.toLowerCase().includes(query.toLowerCase())
                );
                console.log('Patients returned:', filteredPatients);
                resolve(filteredPatients);
            }, 200);
        });
    },

    // Save appointment
    saveAppointment: function(appointmentData) {
        console.log('saveAppointment called with:', appointmentData);
        
        return new Promise((resolve) => {
            setTimeout(() => {
                const newAppointment = {
                    ...appointmentData,
                    id: Date.now().toString(),
                    type: appointmentData.patient_id ? "Patient" : "Other"
                };
                
                dummyAppointments.push(newAppointment);
                
                console.log('Appointment saved:', newAppointment);
                resolve({ 
                    code: 200, 
                    message: "OK",
                    appointment_id: newAppointment.id
                });
            }, 400);
        });
    },

    // Delete appointment
    deleteAppointment: function(appointmentId) {
        console.log('deleteAppointment called with:', appointmentId);
        
        return new Promise((resolve) => {
            setTimeout(() => {
                const index = dummyAppointments.findIndex(apt => apt.id === appointmentId);
                if (index > -1) {
                    dummyAppointments.splice(index, 1);
                    console.log('Appointment deleted:', appointmentId);
                    resolve({ code: 200, message: "OK" });
                } else {
                    console.log('Appointment not found:', appointmentId);
                    resolve({ code: 400, message: "Appointment not found" });
                }
            }, 300);
        });
    },

    // Edit appointment
    editAppointment: function(appointmentData) {
        console.log('editAppointment called with:', appointmentData);
        
        return new Promise((resolve) => {
            setTimeout(() => {
                const index = dummyAppointments.findIndex(apt => apt.id === appointmentData.appointment_id);
                if (index > -1) {
                    dummyAppointments[index] = {
                        ...dummyAppointments[index],
                        title: appointmentData.title,
                        notes: appointmentData.notes,
                        from: appointmentData.from,
                        duration_minutes: appointmentData.duration_minutes,
                        patient_id: appointmentData.patient_id,
                        type: appointmentData.patient_id ? "Patient" : "Other"
                    };
                    
                    console.log('Appointment updated:', dummyAppointments[index]);
                    resolve({ code: 200, message: "OK" });
                } else {
                    console.log('Appointment not found:', appointmentData.appointment_id);
                    resolve({ code: 400, message: "Appointment not found" });
                }
            }, 400);
        });
    }
};