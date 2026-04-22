//
//  prescription_api.js
//  Test
//
//  Created by Hold Apps on 27/3/2026.
//  Copyright © 2026 Hold Apps. All rights reserved.
//

// ==================== BRIDGE API ====================
// Single communication channel for all JS ↔ Swift interactions

let cachedBridge = null;

// Returns:
function ensureBridge(){
    if (cachedBridge) return cachedBridge;
    
    const handler = window.webkit?.messageHandlers?.nativeBridge
    
    if (!handler){
        return null;
    }
    
    cachedBridge = {
        callNative: function(type, payload) {
            const requestId = `req_${Date.now()}_${Math.random().toString(36).slice(2)}`;
            
            handler.postMessage({
                type,
                payload: typeof payload === 'string'
                    ? payload
                    : JSON.stringify(payload ?? {}),
                requestId
            });
            
            return requestId;
        }
    };
    
    return cachedBridge;
}

function callNative(functionName, payload = '') {
    const bridge = ensureBridge();

    if (!bridge) {
        console.log(`[Web Mode] ${functionName} called with:`, payload);
        return false;
    }

    bridge.callNative(functionName, payload);
    return true;
};

// ==================== PUBLIC API ====================
// This is what your HTML calls - ALL methods go through here
const API = {
    uploadLogo: () => callNative('uploadLogo', ''),

    saveConfig: (config) => {
        return callNative('saveConfig', config);
    },

    requestInitialConfig: () => callNative('requestInitialConfig', ''),

    setLogoImage: (base64Data) => {
        if (!base64Data) {
            return;
        }

        const logoImg = document.getElementById('logoImg');
        if (!logoImg) {
            console.warn('setLogoImage called before #logoImg was available');
            return;
        }

        logoImg.src = base64Data;

        if (typeof window.updateTemplate === 'function') {
            window.updateTemplate();
        }
    }
};

