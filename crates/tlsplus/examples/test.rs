use wreq::Client;
use wreq_util::{Emulation,EmulationOS};

#[tokio::main]
async fn main() -> wreq::Result<()> {
    // Build a client
    let client = Client::builder()
        // .emulation()
        .emulation(Emulation::SafariIos18_1_1)
        .build()?;

    // let enum_variants = serde_json::to_string(&).unwrap();

    // Use the API you're already familiar with
    let resp = client.get("https://tls.peet.ws/api/all").send().await?;
    println!("{}", resp.text().await?);

    Ok(())
}