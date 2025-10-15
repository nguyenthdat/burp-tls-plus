uniffi::setup_scaffolding!();

use wreq_util::Emulation;
use strum::VariantArray;

#[uniffi::export]
pub fn get_fingerprints()-> Vec<String> {
    Emulation::VARIANTS.iter().map(|e| format!("{:?}", e)).collect()
}

#[uniffi::export]
pub fn stop_server() -> Option<String> {
    // wreq_util::stop_server();
    None
}