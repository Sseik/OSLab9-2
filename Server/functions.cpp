#include "functions.h"
#include <fileapi.h>
#include <iostream>

#define MAX_TIME 60

map<string, DirectoryInfo> cache;

// Usage: cache[directory] = FindInformation[directory];
DirectoryInfo FindInformation(string& directory) {
	time_t now = time(NULL);
	// Повертаємо інформацію з кешу, якщо попередньому запису не більше MAX_TIME секунд
	if (cache.count(directory) && now - cache[directory].searchTime <= MAX_TIME)
		return cache[directory];
	DirectoryInfo res = {};
	WIN32_FIND_DATAA fileInfo = {};
	HANDLE h = FindFirstFileA((directory + "/*.*").c_str(), &fileInfo);
	if (h != INVALID_HANDLE_VALUE) {
		do {
			string name = fileInfo.cFileName;
			if (name == "." || name == "..")
				continue;
			res.searchTime = now;
			res.quantity++;
			res.size += (fileInfo.nFileSizeHigh * ((long long) MAXDWORD + 1)) + fileInfo.nFileSizeLow;
			string type = "folder";
			int pos = name.rfind('.');
			if (pos != string::npos)
				type = name.substr(pos);
			const FileInfo fi = { name, type, fileInfo.ftCreationTime };
			res.files.push_back(fi);
		} while (FindNextFileA(h, &fileInfo));
	}
	return res;
}

// Format: "C:/path|type"
void GetRequest(string& directory, string& type, HANDLE hPipe) {
	unsigned long bytesRead;
	char buffer[500];
	ReadFile(hPipe, buffer, 500, &bytesRead, NULL);
	string sBuffer(buffer);
	std::cout << sBuffer << std::endl;
	int pos = sBuffer.rfind('|');
	if (pos != string::npos) {
		type = sBuffer.substr(pos + 1);
		directory = string(sBuffer, 0, pos);
	}
	else {
		directory = sBuffer;
		type = ".*";
	}
}

// Type is either a word "folder" or an extension (".txt")
void SendInfo(string& directory, string& type, HANDLE hPipe) {
	cache[directory] = FindInformation(directory);
	char buffer[1000] = "Number of files: ";
	WriteFile(hPipe, buffer, sizeof(buffer), NULL, NULL);
	snprintf(buffer, 1000, "%d\n", cache[directory].quantity);
	WriteFile(hPipe, buffer, sizeof(buffer), NULL, NULL);
	snprintf(buffer, 1000, "Summary size of FILES: %ld\n", cache[directory].size);
	WriteFile(hPipe, buffer, sizeof(buffer), NULL, NULL);
	for (int i = 0, n = cache[directory].files.size(); i < n; i++) {
		if (cache[directory].files[i].type == type || type == ".*") {
			SYSTEMTIME stUTC, stLocal;
			FileTimeToSystemTime(&cache[directory].files[i].creationTime, &stUTC);
			SystemTimeToTzSpecificLocalTime(NULL, &stUTC, &stLocal);
			snprintf(buffer, 1000, "%s %hd-%hd-%hd-%hd-%hd\n", cache[directory].files[i].name.c_str(), stLocal.wYear, stLocal.wMonth, stLocal.wDay, stLocal.wHour, stLocal.wMinute);
			WriteFile(hPipe, buffer, sizeof(buffer), NULL, NULL);
		}
	}
	FlushFileBuffers(hPipe);
}
