const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Inicializar Firebase Admin
admin.initializeApp();

/**
 * Cloud Function para eliminar usuario rechazado
 */
exports.deleteRejectedUser = functions.https.onCall(async (data, context) => {
    try {
        const { userId, reason } = data;
        
        console.log(`🗑️ Eliminando usuario rechazado: ${userId}, Motivo: ${reason}`);
        
        // 1. Eliminar de Firebase Authentication
        await admin.auth().deleteUser(userId);
        console.log(`✅ Usuario eliminado de Authentication: ${userId}`);
        
        // 2. Eliminar datos de Firestore por seguridad
        const db = admin.firestore();
        const batch = db.batch();
        
        // Eliminar de pending_drivers
        const pendingRef = db.collection('pending_drivers').doc(userId);
        batch.delete(pendingRef);
        
        // Eliminar de users
        const usersRef = db.collection('users').doc(userId);
        batch.delete(usersRef);
        
        await batch.commit();
        console.log(`✅ Datos de Firestore limpiados: ${userId}`);
        
        return { 
            success: true, 
            message: `Usuario ${userId} eliminado completamente`,
            deletedFrom: ['authentication', 'firestore'],
            reason: reason
        };
        
    } catch (error) {
        console.error(`❌ Error eliminando usuario: ${error.message}`);
        
        // Si el usuario ya no existe en Auth, no es un error crítico
        if (error.code === 'auth/user-not-found') {
            console.log(`ℹ️ Usuario ya no existía en Authentication: ${data.userId}`);
            return { 
                success: true, 
                message: `Usuario ya no existía en Authentication`,
                deletedFrom: ['firestore']
            };
        }
        
        throw new functions.https.HttpsError(
            'internal', 
            `Error eliminando usuario: ${error.message}`
        );
    }
});