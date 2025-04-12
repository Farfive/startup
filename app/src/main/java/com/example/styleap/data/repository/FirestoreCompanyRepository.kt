package com.example.styleap.data.repository

import com.example.styleap.data.model.Company
import com.example.styleap.data.model.EmployeeInfo
import com.example.styleap.domain.repository.CompanyRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreCompanyRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : CompanyRepository {

    companion object {
        private const val COMPANIES_COLLECTION = "companies"
        // TODO: Define how employees are related (subcollection or field in company doc?)
        // For now, assume an "employees" field (List<Map<String, Any>>) in the Company doc
        // Or a fixed company ID for simplicity in this example
        private const val EXAMPLE_COMPANY_ID = "exampleCompany123"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getCompanyDetails(): Flow<Company?> = callbackFlow {
        // For now, fetches a hardcoded company ID. Adapt if company ID comes from user/context.
        val companyDocRef = firestore.collection(COMPANIES_COLLECTION).document(EXAMPLE_COMPANY_ID)

        val listener = companyDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to company details for ID: $EXAMPLE_COMPANY_ID")
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                try {
                    val company = snapshot.toObject(Company::class.java)
                    Timber.d("Company details loaded/updated: $company")
                    trySend(company).isSuccess
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing company details for ID: $EXAMPLE_COMPANY_ID")
                    trySend(null)
                }
            } else {
                Timber.w("Company document does not exist for ID: $EXAMPLE_COMPANY_ID")
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getCompanyEmployees(): Flow<List<EmployeeInfo>> = callbackFlow {
        // Fetching employees based on the same example company ID
        // This assumes employees are stored in a way queryable by company ID
        // Example: A top-level 'employees' collection with a 'companyId' field.
        val employeesQuery = firestore.collection("employees") // Assuming top-level collection
            .whereEqualTo("companyId", EXAMPLE_COMPANY_ID)

        val listener = employeesQuery.addSnapshotListener { snapshot, error ->
             if (error != null) {
                Timber.e(error, "Error listening to company employees for CompanyID: $EXAMPLE_COMPANY_ID")
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                try {
                    val employees = snapshot.toObjects(EmployeeInfo::class.java)
                    Timber.d("Company employees loaded/updated: ${employees.size} employees")
                    trySend(employees).isSuccess
                } catch (e: Exception) {
                     Timber.e(e, "Error parsing company employees for CompanyID: $EXAMPLE_COMPANY_ID")
                     trySend(emptyList()) // Send empty list on parsing error
                }
            } else {
                 Timber.w("Employee snapshot was null for CompanyID: $EXAMPLE_COMPANY_ID")
                 trySend(emptyList())
            }
        }
        awaitClose { listener.remove() }
    }

    // TODO: Implement addPhoto, setHours, addPriceList methods
    // These would likely involve updating the company document or related collections/storage.
} 