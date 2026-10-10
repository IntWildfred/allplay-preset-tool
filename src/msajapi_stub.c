/* Stub MSAJApi.dll: satisfies the import of alljoyn_java.dll (AllJoyn 16.04a, win10 build) on Windows
   versions where Microsoft's AllJoyn (MSAJApi.dll) was removed. Every call fails with ERROR_NOT_SUPPORTED,
   so AllJoyn's "npipe:" transport (Windows' built-in AJRouter) is unavailable and the library uses its
   own bundled router ("null:" transport) instead. */
#include <windows.h>
#define NS SetLastError(ERROR_NOT_SUPPORTED)
__declspec(dllexport) HANDLE WINAPI AllJoynConnectToBus(PCWSTR spec){ NS; return NULL; }
__declspec(dllexport) BOOL   WINAPI AllJoynCloseBusHandle(HANDLE h){ NS; return FALSE; }
__declspec(dllexport) BOOL   WINAPI AllJoynSendToBus(HANDLE h, const void* b, DWORD n, DWORD* t, void* r){ NS; if(t)*t=0; return FALSE; }
__declspec(dllexport) BOOL   WINAPI AllJoynReceiveFromBus(HANDLE h, void* b, DWORD n, DWORD* t, void* r){ NS; if(t)*t=0; return FALSE; }
__declspec(dllexport) HANDLE WINAPI AllJoynAcceptBusConnection(HANDLE s, HANDLE a){ NS; return NULL; }
__declspec(dllexport) HANDLE WINAPI AllJoynCreateBus(DWORD o, DWORD i, LPSECURITY_ATTRIBUTES sa){ NS; return NULL; }
__declspec(dllexport) BOOL   WINAPI AllJoynEventSelect(HANDLE h, HANDLE e, DWORD t){ NS; return FALSE; }
__declspec(dllexport) BOOL   WINAPI AllJoynEnumEvents(HANDLE h, HANDLE e, DWORD* t){ NS; if(t)*t=0; return FALSE; }
BOOL WINAPI DllMain(HINSTANCE h, DWORD r, LPVOID p){ return TRUE; }
